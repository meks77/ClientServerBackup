package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StartupFileVisitorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BackupManager backupManager;

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private Path backupSet;

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
        assertThat(todoEntry.getWatchedPath()).isEqualTo(backupSet);
    }

    @Test
    public void whenVisitFileFailedThenExceptionIsReportedAndReturnsSkipSubtree() {
        Path expectedPath = mock(Path.class);
        IOException ioException = mock(IOException.class);
        FileVisitResult result = visitor.visitFileFailed(expectedPath, ioException);
        assertThat(result).isEqualTo(FileVisitResult.SKIP_SUBTREE);
        verify(errorReporter).reportError("error while doing initial backup", ioException);

    }
}