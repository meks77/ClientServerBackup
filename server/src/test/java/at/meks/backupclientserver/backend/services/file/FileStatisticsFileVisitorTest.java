package at.meks.backupclientserver.backend.services.file;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class FileStatisticsFileVisitorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private FileStatistics fileStatistics;

    @Mock
    private Logger logger;

    @InjectMocks
    private FileStatisticsFileVisitor visitor;

    @Test
    public void givenRegularFileWhenVisitFileThenFileCounterIsIncremented() {
        Path path = mock(Path.class);
        BasicFileAttributes attrs = mock(BasicFileAttributes.class);

        when(attrs.isRegularFile()).thenReturn(true);

        visitor.visitFile(path, attrs);

        verify(fileStatistics).incrementFileCount();
    }

    @Test
    public void givenRegularFileWhenVisitFileThenSizeInBytesIsIncremented() {
        long expectedSizeIncrement = 14L;
        Path path = mock(Path.class);
        BasicFileAttributes attrs = mock(BasicFileAttributes.class);

        when(attrs.isRegularFile()).thenReturn(true);
        when(attrs.size()).thenReturn(expectedSizeIncrement);

        visitor.visitFile(path, attrs);

        verify(fileStatistics).incrementSizeInBytes(expectedSizeIncrement);
    }

    @Test
    public void givenNotRegularFileWhenVisitFileThenStatisticIsNotTouched() {
        Path path = mock(Path.class);
        BasicFileAttributes attrs = mock(BasicFileAttributes.class);

        when(attrs.isRegularFile()).thenReturn(false);

        visitor.visitFile(path, attrs);

        verifyZeroInteractions(fileStatistics);
    }

    @Test
    public void givenRegularFileWhenVisitFileThenReturnsContinue() {
        Path path = mock(Path.class);
        BasicFileAttributes attrs = mock(BasicFileAttributes.class);

        when(attrs.isRegularFile()).thenReturn(true);

        assertThat(visitor.visitFile(path, attrs)).isEqualTo(FileVisitResult.CONTINUE);
    }

    @Test
    public void givenNotRegularFileWhenVisitFileThenReturnsContinue() {
        Path path = mock(Path.class);
        BasicFileAttributes attrs = mock(BasicFileAttributes.class);

        when(attrs.isRegularFile()).thenReturn(false);

        assertThat(visitor.visitFile(path, attrs)).isEqualTo(FileVisitResult.CONTINUE);
    }

    @Test
    public void whenVisitFileFailedThenReturnSkipSubtree() {
        FileVisitResult result = visitor.visitFileFailed(mock(Path.class), mock(IOException.class));
        assertThat(result).isEqualTo(FileVisitResult.SKIP_SUBTREE);
    }

    @Test
    public void whenVisitFileFailedThenExceptionIsLogged() {
        Path path = mock(Path.class);
        IOException exception = mock(IOException.class);

        visitor.visitFileFailed(path, exception);

        verify(logger).warn(any(), same(path), same(exception));
    }

    @Test
    public void givenStatisticsWhenWithStatisticsThenInstanceWithGivenStatsIsReturned() {
        FileStatisticsFileVisitor visitorFromStatistic = FileStatisticsFileVisitor.withStatistics(fileStatistics);
        BasicFileAttributes attrs = mock(BasicFileAttributes.class);

        when(attrs.isRegularFile()).thenReturn(true);

        visitorFromStatistic.visitFile(mock(Path.class), attrs);

        verify(fileStatistics).incrementFileCount();
        verify(fileStatistics).incrementSizeInBytes(anyLong());
    }
}
