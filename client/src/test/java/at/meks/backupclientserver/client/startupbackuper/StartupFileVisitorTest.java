package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backup.model.Client;
import at.meks.backupclientserver.client.backup.model.EventType;
import at.meks.backupclientserver.client.backup.model.FileChangedEvent;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import io.vertx.core.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StartupFileVisitorTest {

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private FileExcludeService fileExcludeService;

    @Mock
    private EventBus eventBus;

    private final String expectedClient = "expectedClient";

    private StartupFileVisitor visitor;

    @BeforeEach
    void initializeVisitor() {
        visitor = new StartupFileVisitor(expectedClient, eventBus , errorReporter, fileExcludeService);
    }

    @Test
    void whenVisitFileThenCallIsDelegatedToBackupManager() {
        Path expectedPath = mock(Path.class);
        visitor.visitFile(expectedPath, mock(BasicFileAttributes.class));

        verify(eventBus)
                .publish("backup", new FileChangedEvent(new Client(expectedClient), expectedPath, EventType.MODIFIED));
    }

    @Test
    public void whenVisitFileFailedThenExceptionIsReportedAndReturnsSkipSubtree() {
        Path expectedPath = mock(Path.class);
        IOException ioException = mock(IOException.class);
        FileVisitResult result = visitor.visitFileFailed(expectedPath, ioException);
        assertThat(result).isEqualTo(FileVisitResult.SKIP_SUBTREE);
        verify(errorReporter).reportError("skip backup for directory " + expectedPath + " because of error.", ioException);

    }

    @Test
    public void givenExcludedDirWhenPreVisitDirectoryThenReturnsSkipSubtree(@TempDir Path visitedDir) throws IOException {
        when(fileExcludeService.isFileExcludedFromBackup(visitedDir)).thenReturn(true);

        FileVisitResult result = visitor.preVisitDirectory(visitedDir, mock(BasicFileAttributes.class));

        assertThat(result).isEqualTo(FileVisitResult.SKIP_SUBTREE);
    }

    @Test
    public void givenIncludedDirWhenPreVisitDirectoryThenReturnsContinue(@TempDir Path visitedDir) throws IOException {
        FileVisitResult result = visitor.preVisitDirectory(visitedDir, mock(BasicFileAttributes.class));

        assertThat(result).isEqualTo(FileVisitResult.CONTINUE);
    }
}