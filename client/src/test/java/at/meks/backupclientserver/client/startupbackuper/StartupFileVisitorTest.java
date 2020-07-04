package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
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
    private BackupManager backupManager;

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private FileExcludeService fileExcludeService;

    @InjectMocks
    private StartupFileVisitor visitor;

    @Test
    public void whenVisitFileThenCallIsDelegatedToBackupManager() {
        Path expectedPath = mock(Path.class);
        visitor.visitFile(expectedPath, mock(BasicFileAttributes.class));

        ArgumentCaptor<TodoEntry> captor = forClass(TodoEntry.class);
        verify(backupManager).addForBackup(captor.capture());
        TodoEntry todoEntry = captor.getValue();
        assertThat(todoEntry.getChangedFile()).isEqualTo(expectedPath);
        assertThat(todoEntry.getType()).isEqualTo(PathChangeType.MODIFIED);
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