package at.meks.backupclientserver.client.filewatcher;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileVisitorTest {

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private FileExcludeService fileExcludeService;

    @Mock
    private Consumer<Path> directoryConsumer;

    @InjectMocks
    private FileVisitor visitor;

    @Test
    public void givenExcludedDirWhenPreVisitDirectoryThenReturnsSkipSubtree(@TempDir Path ignoredPath) {
        when(fileExcludeService.isFileExcludedFromBackup(ignoredPath)).thenReturn(true);

        FileVisitResult result = visitor.preVisitDirectory(ignoredPath, mock(BasicFileAttributes.class));

        assertThat(result).isEqualTo(FileVisitResult.SKIP_SUBTREE);
    }

    @Test
    public void givenExcludedDirWhenPreVisitDirectoryThenDirConsumerIsNotInvoked(@TempDir Path excludedPath) {
        when(fileExcludeService.isFileExcludedFromBackup(excludedPath)).thenReturn(true);

        visitor.preVisitDirectory(excludedPath, mock(BasicFileAttributes.class));

        verifyNoInteractions(directoryConsumer);
    }

    @Test
    public void givenIncludedDirWhenPreVisitDirectoryThenReturnsContinue(@TempDir Path visitedPath) {
        when(fileExcludeService.isFileExcludedFromBackup(visitedPath)).thenReturn(false);

        FileVisitResult result = visitor.preVisitDirectory(visitedPath, mock(BasicFileAttributes.class));

        assertThat(result).isEqualTo(FileVisitResult.CONTINUE);
    }

    @Test
    public void givenIncludedDirWhenPreVisitDirectoryThenDirConsumerIsInvoked(@TempDir Path visitedPath) {
        when(fileExcludeService.isFileExcludedFromBackup(visitedPath)).thenReturn(false);

        visitor.preVisitDirectory(visitedPath, mock(BasicFileAttributes.class));

        verify(directoryConsumer).accept(visitedPath);
    }

    @Test
    public void verifyVisitFileFailedReturnsContinue() {
        FileVisitResult result = visitor.visitFileFailed(mock(Path.class), mock(IOException.class));
        assertThat(result).isEqualTo(FileVisitResult.CONTINUE);
    }

    @Test
    public void whenVisitFileFailedThenErrorReporterIsInvoked() {
        IOException expectedException = mock(IOException.class);
        Path visitedPath = mock(Path.class);
        visitor.visitFileFailed(visitedPath, expectedException);
        verify(errorReporter).reportError("error start listening to file changes of " + visitedPath, expectedException);
    }
}