package at.meks.backupclientserver.client.filewatcher;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class FileVisitorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private FileExcludeService fileExcludeService;

    @Mock
    private Consumer<Path> directoryConsumer;

    @InjectMocks
    private FileVisitor visitor;

    @Test
    public void givenExcludedDirWhenPreVisitDirectoryThenReturnsSkipSubtree() {
        Path visitedPath = Paths.get("whatever");
        when(fileExcludeService.isFileExcludedFromBackup(visitedPath)).thenReturn(true);

        FileVisitResult result = visitor.preVisitDirectory(visitedPath, mock(BasicFileAttributes.class));

        assertThat(result).isEqualTo(FileVisitResult.SKIP_SUBTREE);
    }

    @Test
    public void givenExcludedDirWhenPreVisitDirectoryThenDirConsumerIsNotInvoked() {
        Path visitedPath = Paths.get("whatever");
        when(fileExcludeService.isFileExcludedFromBackup(visitedPath)).thenReturn(true);

        visitor.preVisitDirectory(visitedPath, mock(BasicFileAttributes.class));

        verifyZeroInteractions(directoryConsumer);
    }

    @Test
    public void givenIncludedDirWhenPreVisitDirectoryThenReturnsContinue() {
        Path visitedPath = Paths.get("whatever");

        FileVisitResult result = visitor.preVisitDirectory(visitedPath, mock(BasicFileAttributes.class));

        assertThat(result).isEqualTo(FileVisitResult.CONTINUE);
    }

    @Test
    public void givenIncludedDirWhenPreVisitDirectoryThenDirConsumerIsInvoked() {
        Path visitedPath = Paths.get("whatever");

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