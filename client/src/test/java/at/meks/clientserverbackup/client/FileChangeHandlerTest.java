package at.meks.clientserverbackup.client;

import at.meks.clientserverbackup.client.backupmanager.BackupManager;
import at.meks.clientserverbackup.client.backupmanager.PathChangeType;
import at.meks.clientserverbackup.client.backupmanager.TodoEntry;
import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class FileChangeHandlerTest {

    @Mock
    private Logger logger;

    @Mock
    private BackupManager backupManager;

    @InjectMocks
    private FileChangeHandler handler = new FileChangeHandler();

    @Test
    public void givenEntryCreatedWhenFileChangedThenBackupManagerCreatedIsInvoked() {
        Path changedFile = mock(Path.class);
        Path watchedPath = mock(Path.class);
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.CREATED, changedFile, watchedPath);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.ENTRY_CREATE, changedFile);

        verify(backupManager).addForBackup(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenEntryModifiedWhenFileChangedThenBackupManagerMoodifiedIsInvoked() {
        Path changedFile = mock(Path.class);
        Path watchedPath = mock(Path.class);
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.MODIFIED, changedFile, watchedPath);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.ENTRY_MODIFY, changedFile);

        verify(backupManager).addForBackup(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenEntryDeletedWhenFileChangedThenBackupManagerDeletedIsInvoked() {
        Path changedFile = mock(Path.class);
        Path watchedPath = mock(Path.class);
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.DELETED, changedFile, watchedPath);
        Assertions.assertThat(changedFile).isNotEqualTo(watchedPath);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.ENTRY_DELETE, changedFile);
        verify(backupManager).addForBackup(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenUnexpectedEntryWhenFileChangedThenLogIsWritten() {
        Path changedFile = mock(Path.class);
        Path watchedPath = mock(Path.class);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.OVERFLOW, changedFile);

        verifyZeroInteractions(backupManager);
        verify(logger).error(anyString(), any(WatchEvent.Kind.class));
    }
}
