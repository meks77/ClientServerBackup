package at.meks.backupclientserver.client.filechangehandler;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileChangeHandlerImplTest {

    @Spy
    private Logger logger = LoggerFactory.getLogger(FileChangeHandlerImpl.class);

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private BackupManager backupManager;

    @InjectMocks
    private FileChangeHandlerImpl handler = new FileChangeHandlerImpl();

    @Test
    public void givenEntryCreatedWhenFileChangedThenBackupManagerCreatedIsInvoked() throws IOException {
        Path changedFile = createTemporaryFile();
        Path watchedPath = mock(Path.class);
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.CREATED, changedFile, watchedPath);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.ENTRY_CREATE, changedFile);

        verifyBackupManagerInvocation(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenEntryModifiedWhenFileChangedThenBackupManagerMoodifiedIsInvoked() throws IOException {
        Path changedFile = createTemporaryFile();
        Path watchedPath = mock(Path.class);
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.MODIFIED, changedFile, watchedPath);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.ENTRY_MODIFY, changedFile);

        verifyBackupManagerInvocation(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    private Path createTemporaryFile() throws IOException {
        return Files.createTempFile(TestDirectoryProvider.createTempDirectory(), "unitTest", ".txt");
    }

    private void verifyBackupManagerInvocation(TodoEntry expectedTodoEntry) {
        ArgumentCaptor<TodoEntry> argumentCaptor = ArgumentCaptor.forClass(TodoEntry.class);
        verify(backupManager, timeout(1200)).addForBackup(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualsToByComparingFields(expectedTodoEntry);
    }

    @Test
    public void givenUnexpectedEntryWhenFileChangedThenLogIsWritten() {
        Path changedFilePath = mock(Path.class);
        File changedFile = mock(File.class);
        when(changedFilePath.toFile()).thenReturn(changedFile);
        when(changedFile.isFile()).thenReturn(true);
        Path watchedPath = mock(Path.class);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.OVERFLOW, changedFilePath);

        verifyZeroInteractions(backupManager);
        String message = "unknown WatchEvent.Kind " + StandardWatchEventKinds.OVERFLOW ;
        ArgumentCaptor<ClientBackupException> captor = ArgumentCaptor.forClass(ClientBackupException.class);
        verify(errorReporter).reportError(eq(message), captor.capture());
        assertThat(captor.getValue().getMessage()).isEqualTo(message);
    }

    @Test
    public void givenWritingAFileWhileAddForBackupThenBackupOfFileIsDoneAfterWritingFinished() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path hugeFile = Files.createTempFile(backupSetPath, "hugeFile", ".txt");

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<?> writerFuture = executorService.submit(() -> {
            long startTime = System.currentTimeMillis();
            long writeTimeDurationInMs = 3000;
            while (System.currentTimeMillis() < startTime + writeTimeDurationInMs) {
                try {
                    FileUtils.write(hugeFile.toFile(), "s", "utf8");
                    handler.fileChanged(backupSetPath, StandardWatchEventKinds.ENTRY_MODIFY, hugeFile);
                    Thread.sleep(100);
                } catch (Exception e) {
                    Assert.fail("couldn't write file. Excepion occured: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        verify(backupManager, timeout(1000).times(0)).addForBackup(any());
        verify(backupManager, timeout(5000).times(1)).addForBackup(any());
        assertThat(writerFuture.isDone()).isTrue();
    }

    @Test
    public void givenRenamedDirectoryWhenFileChangedThenAllFilesOfDirAreBackuped() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path renamedFolder = backupSetPath.resolve("folder1");
        Files.createDirectories(renamedFolder);
        Path file1 = Files.createFile(renamedFolder.resolve("file1.txt"));
        Path file2 = Files.createFile(renamedFolder.resolve("file2.txt"));
        Path file3 = Files.createFile(renamedFolder.resolve("file3.txt"));

        handler.fileChanged(backupSetPath, StandardWatchEventKinds.ENTRY_CREATE, renamedFolder);

        ArgumentCaptor<TodoEntry> captor = ArgumentCaptor.forClass(TodoEntry.class);
        verify(backupManager, timeout(2000).times(3)).addForBackup(captor.capture());
        List<TodoEntry> entries = captor.getAllValues();
        assertThat(entries).usingElementComparator(this::compareTodoEntries)
                .containsOnly(new TodoEntry(PathChangeType.CREATED, file1, backupSetPath),
                        new TodoEntry(PathChangeType.CREATED, file2, backupSetPath),
                        new TodoEntry(PathChangeType.CREATED, file3, backupSetPath));
    }

    private int compareTodoEntries(TodoEntry o1, TodoEntry o2) {
        boolean entriesAreEqual = Objects.equals(o1.getChangedFile(), o2.getChangedFile()) &&
                o1.getType() == o2.getType() &&
                Objects.equals(o1.getWatchedPath(), o2.getWatchedPath());
        return Boolean.compare(entriesAreEqual, true);
    }

    @Test
    public void givenKindDeltedWhenFileChangedThenPathChangeTypeDeletedIsPutToBackupManager() {
        handler.fileChanged(Paths.get("whatever"), StandardWatchEventKinds.ENTRY_DELETE, Paths.get("notExistingFile.txt"));
        ArgumentCaptor<TodoEntry> captor = ArgumentCaptor.forClass(TodoEntry.class);
        verify(backupManager, timeout(2000)).addForBackup(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(PathChangeType.DELETED);
    }
}
