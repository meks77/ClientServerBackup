package at.meks.backupclientserver.client.filechangehandler;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileChangeHandlerImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private Logger logger;

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private BackupManager backupManager;

    @Mock
    private FileExcludeService fileExcludeService;

    @InjectMocks
    private FileChangeHandlerImpl handler = new FileChangeHandlerImpl();

    @Test
    public void givenEntryCreatedWhenFileChangedThenBackupManagerCreatedIsInvoked() throws IOException {
        Path changedFile = createTemporaryFile();
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.CREATED, changedFile);

        handler.fileChanged(StandardWatchEventKinds.ENTRY_CREATE, changedFile);

        verifyBackupManagerInvocation(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenEntryModifiedWhenFileChangedThenBackupManagerMoodifiedIsInvoked() throws IOException {
        Path changedFile = createTemporaryFile();
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.MODIFIED, changedFile);

        handler.fileChanged(StandardWatchEventKinds.ENTRY_MODIFY, changedFile);

        verifyBackupManagerInvocation(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    private Path createTemporaryFile() throws IOException {
        return Files.createTempFile(tempDir, "unitTest", ".txt");
    }

    private void verifyBackupManagerInvocation(TodoEntry expectedTodoEntry) {
        ArgumentCaptor<TodoEntry> argumentCaptor = ArgumentCaptor.forClass(TodoEntry.class);
        verify(backupManager, timeout(1200)).addForBackup(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(expectedTodoEntry);
    }

    @Test
    public void givenUnexpectedEntryWhenFileChangedThenLogIsWritten() {
        Path changedFilePath = mock(Path.class);
        File changedFile = mock(File.class);
        when(changedFilePath.toFile()).thenReturn(changedFile);
        when(changedFile.isFile()).thenReturn(true);

        handler.fileChanged(StandardWatchEventKinds.OVERFLOW, changedFilePath);

        verifyNoInteractions(backupManager);
        String message = "unknown WatchEvent.Kind " + StandardWatchEventKinds.OVERFLOW ;
        ArgumentCaptor<ClientBackupException> captor = ArgumentCaptor.forClass(ClientBackupException.class);
        verify(errorReporter).reportError(eq(message), captor.capture());
        assertThat(captor.getValue().getMessage()).isEqualTo(message);
    }

    @Test
    public void givenWritingAFileWhileAddForBackupThenBackupOfFileIsDoneAfterWritingFinished(@TempDir Path backupSetPath)
            throws IOException {
        Path hugeFile = Files.createTempFile(backupSetPath, "hugeFile", ".txt");

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<?> writerFuture = executorService.submit(() -> {
            long startTime = System.currentTimeMillis();
            long writeTimeDurationInMs = 3000;
            while (System.currentTimeMillis() < startTime + writeTimeDurationInMs) {
                try {
                    FileUtils.write(hugeFile.toFile(), "s", "utf8");
                    handler.fileChanged(StandardWatchEventKinds.ENTRY_MODIFY, hugeFile);
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.error("error while writing file for test", e);
                    fail("couldn't write file. Excepion occured: " + e.getMessage());
                }
            }
        });
        Awaitility.await().pollDelay(1, TimeUnit.SECONDS).until(() -> !writerFuture.isDone());
        verify(backupManager, never()).addForBackup(any());
        verify(backupManager, timeout(5000).times(1)).addForBackup(any());
        assertThat(writerFuture.isDone()).isTrue();
    }

    @Test
    public void givenRenamedDirectoryWhenFileChangedThenAllFilesOfDirAreBackuped(@TempDir Path backupSetPath) throws IOException {
        Path renamedFolder = backupSetPath.resolve("folder1");
        Files.createDirectories(renamedFolder);
        Path file1 = Files.createFile(renamedFolder.resolve("file1.txt"));
        Path file2 = Files.createFile(renamedFolder.resolve("file2.txt"));
        Path file3 = Files.createFile(renamedFolder.resolve("file3.txt"));

        handler.fileChanged(StandardWatchEventKinds.ENTRY_CREATE, renamedFolder);

        ArgumentCaptor<TodoEntry> captor = ArgumentCaptor.forClass(TodoEntry.class);
        verify(backupManager, timeout(2000).times(3)).addForBackup(captor.capture());
        List<TodoEntry> entries = captor.getAllValues();
        assertThat(entries).usingElementComparator(this::compareTodoEntries)
                .containsOnly(new TodoEntry(PathChangeType.CREATED, file1),
                        new TodoEntry(PathChangeType.CREATED, file2),
                        new TodoEntry(PathChangeType.CREATED, file3));
    }

    private int compareTodoEntries(TodoEntry o1, TodoEntry o2) {
        boolean entriesAreEqual = Objects.equals(o1.getChangedFile(), o2.getChangedFile()) &&
                o1.getType() == o2.getType();
        return Boolean.compare(entriesAreEqual, true);
    }

    @Test
    public void givenKindDeltedWhenFileChangedThenPathChangeTypeDeletedIsPutToBackupManager() {
        handler.fileChanged(StandardWatchEventKinds.ENTRY_DELETE, Paths.get("notExistingFile.txt"));
        ArgumentCaptor<TodoEntry> captor = ArgumentCaptor.forClass(TodoEntry.class);
        verify(backupManager, timeout(2000)).addForBackup(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(PathChangeType.DELETED);
    }
}
