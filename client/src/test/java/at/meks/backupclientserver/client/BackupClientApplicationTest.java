package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.filechangehandler.FileChangeHandlerImpl;
import at.meks.backupclientserver.client.filewatcher.FileWatcher;
import at.meks.backupclientserver.client.startupbackuper.StartupBackuper;
import io.quarkus.runtime.Quarkus;
import lombok.SneakyThrows;
import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, JMockitExtension.class})
public class BackupClientApplicationTest {

    @Mock
    private FileChangeHandlerImpl fileChangeHandler;

    @Mock
    private ApplicationConfig applicationConfig;

    @Mock
    private FileWatcher fileWatcher;

    @Mock
    private StartupBackuper startupBackuper;

    @InjectMocks
    private BackupClientApplication application;

    @BeforeEach
    void mockQuarkus() {
        new MockUp<Quarkus>() {
            @mockit.Mock
            public void waitForExit() {

            }
        };
    }

    @Test
    void givenConfigurePathesWhenRunAreSetAtWatcher() {
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

    @SneakyThrows
    private void invokePrivateRunMethod() {
        application.run();
    }

    @Test
    void whenRunThenFileChangeHandlerIsSetAtFileWatcher() {
        invokePrivateRunMethod();
        verify(fileWatcher).setOnChangeConsumer(fileChangeHandler);
    }

    @Test
    void whenRunThenFileWatcherIsStarted() {
        invokePrivateRunMethod();
        verify(fileWatcher).startWatching();
    }

    @Test
    void whenRunThenStartupBackuperIsInvoked() {
        Path[] paths = createPathsArray();
        when(applicationConfig.getBackupedDirs()).thenReturn(paths);

        invokePrivateRunMethod();
        verify(startupBackuper, timeout(1000)).backupIfNecessary(paths);
    }

}
