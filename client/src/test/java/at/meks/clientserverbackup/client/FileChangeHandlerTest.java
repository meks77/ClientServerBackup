package at.meks.clientserverbackup.client;

import at.meks.clientserverbackup.client.backupmanager.BackupManager;
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
        Path path = mock(Path.class);
        handler.fileChanged(StandardWatchEventKinds.ENTRY_CREATE, path);
        verify(backupManager).created(same(path));
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenEntryModifiedWhenFileChangedThenBackupManagerMoodifiedIsInvoked() {
        Path path = mock(Path.class);
        handler.fileChanged(StandardWatchEventKinds.ENTRY_MODIFY, path);
        verify(backupManager).modified(same(path));
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenEntryDeletedWhenFileChangedThenBackupManagerDeletedIsInvoked() {
        Path path = mock(Path.class);
        handler.fileChanged(StandardWatchEventKinds.ENTRY_DELETE, path);
        verify(backupManager).deleted(same(path));
        verifyNoMoreInteractions(backupManager);
    }

    @Test
    public void givenUnexpectedEntryWhenFileChangedThenLogIsWritten() {
        Path path = mock(Path.class);
        handler.fileChanged(StandardWatchEventKinds.OVERFLOW, path);
        verifyZeroInteractions(backupManager);
        verify(logger).error(anyString(), any(WatchEvent.Kind.class));
    }
}
