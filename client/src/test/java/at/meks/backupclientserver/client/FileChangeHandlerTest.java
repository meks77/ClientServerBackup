package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private FileChangeHandlerImpl handler = new FileChangeHandlerImpl();

    @Test
    public void givenEntryCreatedWhenFileChangedThenBackupManagerCreatedIsInvoked() {
        Path changedFile = mock(Path.class);
        Path watchedPath = mock(Path.class);
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.CREATED, changedFile, watchedPath);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.ENTRY_CREATE, changedFile);

        verifyBackupManagerInvocation(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenEntryModifiedWhenFileChangedThenBackupManagerMoodifiedIsInvoked() {
        Path changedFile = mock(Path.class);
        Path watchedPath = mock(Path.class);
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.MODIFIED, changedFile, watchedPath);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.ENTRY_MODIFY, changedFile);

        verifyBackupManagerInvocation(expectedTodoEntry);
        verifyNoMoreInteractions(backupManager);
    }

    private void verifyBackupManagerInvocation(TodoEntry expectedTodoEntry) {
        ArgumentCaptor<TodoEntry> argumentCaptor = ArgumentCaptor.forClass(TodoEntry.class);
        verify(backupManager).addForBackup(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualsToByComparingFields(expectedTodoEntry);
    }

    @Test
    public void givenEntryDeletedWhenFileChangedThenBackupManagerDeletedIsInvoked() {
        Path changedFile = mock(Path.class);
        Path watchedPath = mock(Path.class);
        TodoEntry expectedTodoEntry = new TodoEntry(PathChangeType.DELETED, changedFile, watchedPath);
        assertThat(changedFile).isNotEqualTo(watchedPath);

        handler.fileChanged(watchedPath, StandardWatchEventKinds.ENTRY_DELETE, changedFile);
        verifyBackupManagerInvocation(expectedTodoEntry);
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
