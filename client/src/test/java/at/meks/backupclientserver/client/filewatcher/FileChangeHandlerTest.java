package at.meks.backupclientserver.client.filewatcher;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.SystemService;
import at.meks.backupclientserver.client.backup.model.BackupCandidateService;
import at.meks.backupclientserver.client.backup.model.Client;
import at.meks.backupclientserver.client.backup.model.EventType;
import at.meks.backupclientserver.client.backup.model.FileChangedEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
@Slf4j
class FileChangeHandlerTest {

    @InjectMock
    ErrorReporter errorReporter;

    @InjectMock
    SystemService systemService;

    @InjectMock
    BackupCandidateService backupCandidateService;

    @Inject
    FileChangeHandler handler;

    private Path tempFile;

    @SneakyThrows
    @AfterEach
    void deleteTempFile() {
        if (tempFile != null) {
            Files.delete(tempFile);
        }
    }

    @Test
    void givenEntryCreatedWhenFileChangedThenBackupManagerCreatedIsInvoked() throws IOException {
        Path changedFile = createTemporaryFile();
        String expectedClientId = UUID.randomUUID().toString();
        when(systemService.getHostname()).thenReturn(expectedClientId);
        FileChangedEvent expectedTodoEntry = new FileChangedEvent(new Client(expectedClientId), changedFile, EventType.CREATED);

        handler.fileChanged(StandardWatchEventKinds.ENTRY_CREATE, changedFile);

        verifyBackupManagerInvocation(expectedTodoEntry);
        verifyNoMoreInteractions(backupCandidateService);
    }

    @Test
    void givenEntryModifiedWhenFileChangedThenBackupManagerMoodifiedIsInvoked() throws IOException {
        Path changedFile = createTemporaryFile();
        String expectedClientId = UUID.randomUUID().toString();
        when(systemService.getHostname()).thenReturn(expectedClientId);
        FileChangedEvent expectedTodoEntry = new FileChangedEvent(new Client(expectedClientId), changedFile, EventType.MODIFIED);

        handler.fileChanged(StandardWatchEventKinds.ENTRY_MODIFY, changedFile);

        verifyBackupManagerInvocation(expectedTodoEntry);
        verifyNoMoreInteractions(backupCandidateService);
    }

    private Path createTemporaryFile() throws IOException {
        tempFile = Files.createTempFile("unitTest", ".txt");
        return tempFile;
    }

    private void verifyBackupManagerInvocation(FileChangedEvent expectedEvent) {
        verify(backupCandidateService, timeout(600)).fileRegisteredForBackup(expectedEvent);
    }

    @Test
    void givenUnexpectedEntryWhenFileChangedThenLogIsWritten() {
        Path changedFilePath = mock(Path.class);
        File changedFile = mock(File.class);
        when(changedFilePath.toFile()).thenReturn(changedFile);
        when(changedFile.isFile()).thenReturn(true);

        handler.fileChanged(StandardWatchEventKinds.OVERFLOW, changedFilePath);

        verifyNoInteractions(backupCandidateService);
        String message = "unknown WatchEvent.Kind " + StandardWatchEventKinds.OVERFLOW ;
        ArgumentCaptor<ClientBackupException> captor = ArgumentCaptor.forClass(ClientBackupException.class);
        verify(errorReporter).reportError(eq(message), captor.capture());
        assertThat(captor.getValue().getMessage()).isEqualTo(message);
    }

    @Test
    void givenWritingAFileWhileAddForBackupThenBackupOfFileIsDoneAfterWritingFinished(@TempDir Path backupSetPath)
            throws IOException {
        final Path file = Files.createTempFile(backupSetPath, "hugeFile", ".txt");
        handler.fileChanged(StandardWatchEventKinds.ENTRY_MODIFY, file);
        Awaitility.await().pollDelay(100, TimeUnit.MILLISECONDS).until(() -> true);
        verify(backupCandidateService, never()).fileRegisteredForBackup(any());
        verify(backupCandidateService, timeout(5000).times(1)).fileRegisteredForBackup(any());
    }

    @Test
    void givenRenamedDirectoryWhenFileChangedThenAllFilesOfDirAreBackuped(@TempDir Path backupSetPath) throws IOException {
        String expectedClientId = UUID.randomUUID().toString();
        when(systemService.getHostname()).thenReturn(expectedClientId);

        Path renamedFolder = backupSetPath.resolve("folder1");
        Files.createDirectories(renamedFolder);
        Path file1 = Files.createFile(renamedFolder.resolve("file1.txt"));
        Path file2 = Files.createFile(renamedFolder.resolve("file2.txt"));
        Path file3 = Files.createFile(renamedFolder.resolve("file3.txt"));

        handler.fileChanged(StandardWatchEventKinds.ENTRY_CREATE, renamedFolder);

        ArgumentCaptor<FileChangedEvent> captor = ArgumentCaptor.forClass(FileChangedEvent.class);
        verify(backupCandidateService, timeout(2000).times(3)).fileRegisteredForBackup(captor.capture());
        List<FileChangedEvent> entries = captor.getAllValues();
        Client expectedClient = new Client(expectedClientId);
        assertThat(entries)
                .containsOnly(new FileChangedEvent(expectedClient, file1, EventType.CREATED),
                        new FileChangedEvent(expectedClient, file2, EventType.CREATED),
                        new FileChangedEvent(expectedClient, file3, EventType.CREATED));
    }

    @Test
    void givenKindDeltedWhenFileChangedThenPathChangeTypeDeletedIsPutToBackupManager() {
        handler.fileChanged(StandardWatchEventKinds.ENTRY_DELETE, Paths.get("notExistingFile.txt"));
        ArgumentCaptor<FileChangedEvent> captor = ArgumentCaptor.forClass(FileChangedEvent.class);
        verify(backupCandidateService, timeout(2000)).fileRegisteredForBackup(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventType.DELETED);
    }
}
