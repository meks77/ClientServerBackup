package at.meks.backup.client.usecases;

import at.meks.backup.client.model.DirectoryForBackup;
import at.meks.backup.client.model.Events;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.waitAtMost;

@QuarkusTest
@Slf4j
class FileChangeListenerTest {

    @Inject
    FileChangeListener fileChangeListener;

    @InjectMock(convertScopes = true)
    Events events;

    @SneakyThrows
    @RepeatedTest(5)
    void fileInRootDirChanged(@TempDir Path directory) {
        Path testedFile = createFile(directory.resolve("testfile.txt"));
        log.trace("start setup listener");
        setupListener(directory);
        Thread.sleep(50);

        Files.writeString(testedFile, "y");

        verifyOnlyOneEventIsFired(testedFile);
    }

    @SneakyThrows
    @RepeatedTest(5)
    void fileInSubDirChanged(@TempDir Path directory) {
        Path testedFile = createFile(directory.resolve("subDir").resolve("testfile.txt"));
        log.trace("start setup listener");
        setupListener(directory);

        Files.writeString(testedFile, "y");

        verifyOnlyOneEventIsFired(testedFile);
    }

    @SneakyThrows
    private static Path createFile(Path file) {
        Files.createDirectories(file.getParent());
        Files.writeString(Files.createFile(file), "x");
        return file;
    }

    private void setupListener(Path directory) {
        fileChangeListener.listenToChangesAsync(new DirectoryForBackup(directory));
        waitForListenerToBeInitialized();
    }

    private static void waitForListenerToBeInitialized() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void verifyOnlyOneEventIsFired(Path testedFile) {
        waitAtMost(Duration.ofSeconds(2))
                .and()
                .pollDelay(Duration.ofMillis(100))
                .and()
                .atLeast(Duration.ofMillis(100))
                .untilAsserted(() ->
                        Mockito.verify(events).fireFileChanged(new Events.FileChangedEvent(testedFile)));
        await()
                .atLeast(Duration.ofMillis(100))
                .and()
                .pollDelay(Duration.ofMillis(100))
                        .untilAsserted(() -> Mockito.verifyNoMoreInteractions(events));
    }

}