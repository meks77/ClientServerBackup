package at.meks.backupclientserver.client.excludes;

import at.meks.backupclientserver.client.ApplicationConfig;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class FileExcludeServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ApplicationConfig applicationConfig;

    @Mock
    private SearchStringPathMatcher searchStringPathMatcher;

    @InjectMocks
    private FileExcludeService service = new FileExcludeService();

    @Before
    public void init() {
        when(applicationConfig.getExcludedFileExtensions()).thenReturn(Sets.newHashSet("tmp", "dmp", "lock"));
        service.initExcludedExtensions();
    }

    @Test
    public void verifyThatExcludedExtensionsAreIntialized() throws NoSuchMethodException {
        assertThat(FileExcludeService.class.getDeclaredMethod("initExcludedExtensions").isAnnotationPresent(Inject.class))
                .as("is annotation for initialization").isTrue();
    }

    @Test
    public void givenExcludedExtensionWhenIsFileExcludedFromBackupThenReturnsTrue() throws IOException {
        Path file = createTestFile("somename.dmp");
        assertThat(service.isFileExcludedFromBackup(file)).isTrue();
    }

    private Path createTestFile(String fileName) throws IOException {
        Path tempDirectory = TestDirectoryProvider.createTempDirectory();
        return Files.createFile(tempDirectory.resolve(fileName));
    }

    @Test
    public void givenMixedCaseExcludedExtensionWhenIsFileExcludedFromBackupThenReturnsTrue() throws IOException {
        Path file = createTestFile("somename.DmP");
        assertThat(service.isFileExcludedFromBackup(file)).isTrue();
    }

    @Test
    public void givenNotExcludedExtensionWhenIsFileExcludedFromBackupThenReturnsFalse() throws IOException {
        Path file = createTestFile("somename.txt");
        assertThat(service.isFileExcludedFromBackup(file)).isFalse();
    }

    @Test
    public void givenFileWithouFileExtensionWhenThenReturnsFalse() throws IOException {
        Path file = createTestFile("fileWithoutExtension");
        assertThat(service.isFileExcludedFromBackup(file)).isFalse();
    }

    @Test
    public void givenEmptyExcludesThenPathMatcherIsNeverInvoked() throws IOException {
        Path file = createTestFile("testfile.txt");

        service.initExcludedExtensions();
        assertThat(service.isFileExcludedFromBackup(file)).isFalse();
        verifyZeroInteractions(searchStringPathMatcher);
    }

    @Test
    public void givenPathWhenMatchesToExcludedPathThenReturnsTrue() throws IOException {
        Path file = createTestFile("testfile.txt");
        when(applicationConfig.getExcludes()).thenReturn(Sets.newHashSet("whatever"));
        when(searchStringPathMatcher.matches("whatever", file)).thenReturn(true);

        service.initExcludedExtensions();
        assertThat(service.isFileExcludedFromBackup(file)).isTrue();
    }

    @Test
    public void givenMoreExcludesWhenMatchesThenPathMatcherIsInvokedOncePerExclude() throws IOException {
        Path file = createTestFile("testfile.dat");
        HashSet<String> excludes = Sets.newHashSet("whatever", "whenever", "wherever");
        when(applicationConfig.getExcludes()).thenReturn(excludes);

        service.initExcludedExtensions();
        service.isFileExcludedFromBackup(file);

        excludes.forEach(exclude -> verify(searchStringPathMatcher).matches(exclude, file));
    }

}