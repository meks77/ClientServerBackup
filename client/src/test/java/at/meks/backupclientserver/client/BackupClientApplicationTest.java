package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.filechangehandler.FileChangeHandlerImpl;
import at.meks.backupclientserver.client.startupbackuper.StartupBackuper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BackupClientApplicationTest {

    @Mock
    private FileChangeHandlerImpl fileChangeHandler;

    @Mock
    private ApplicationConfig applicationConfig;

    @Mock
    private FileWatcher fileWatcher;

    @Mock
    private StartupBackuper startupBackuper;

    @Mock
    private HeartBeatReporter heartBeatReporter;

    @InjectMocks
    private BackupClientApplication application;

    @Test
    public void givenConfigurePathesWhenRunAreSetAtWatcher() throws InvocationTargetException, IllegalAccessException,
            NoSuchMethodException {
        Path[] paths = createPathsArray();
        when(applicationConfig.getBackupedDirs()).thenReturn(paths);

        invokePrivateRunMethod();

        ArgumentCaptor<Path[]> argumentCaptor = ArgumentCaptor.forClass(Path[].class);
        verify(fileWatcher).setPathsToWatch(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isSameAs(paths);
    }

    private Path[] createPathsArray() {
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);
        Path path3 = mock(Path.class);
        return new Path[]{path1, path2, path3};
    }

    private void invokePrivateRunMethod() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method run = application.getClass().getDeclaredMethod("run");
        run.setAccessible(true);
        run.invoke(application);
    }

    @Test
    public void whenRunThenFileChangeHandlerIsSetAtFileWatcher() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        invokePrivateRunMethod();
        verify(fileWatcher).setOnChangeConsumer(fileChangeHandler);
    }

    @Test
    public void whenRunThenFileWatcherIsStarted() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        invokePrivateRunMethod();
        verify(fileWatcher).startWatching();
    }

    @Test
    public void whenRunThenStartupBackuperIsInvoked() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Path[] paths = createPathsArray();
        when(applicationConfig.getBackupedDirs()).thenReturn(paths);

        invokePrivateRunMethod();
        verify(startupBackuper, timeout(1000)).backupIfNecessary(paths);
    }

    @Test
    public void whenRunThenHeartbeatReporterIsStarted() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        invokePrivateRunMethod();
        verify(heartBeatReporter).startHeartbeatReporting();
    }
}
