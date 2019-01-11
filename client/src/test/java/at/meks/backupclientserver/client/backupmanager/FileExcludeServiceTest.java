package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ApplicationConfig;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

public class FileExcludeServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ApplicationConfig applicationConfig;

    @InjectMocks
    private FileExcludeService service = new FileExcludeService();

    @Before
    public void init() {
        Mockito.when(applicationConfig.getExcludedFileExtensions()).thenReturn(Sets.newHashSet("tmp", "dmp", "lock"));
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
    public void givenDirectoryWhenIsFileExcludedFromBackupThenReturnsFalse() {
        Path folder = TestDirectoryProvider.createTempDirectory();
        assertThat(service.isFileExcludedFromBackup(folder)).isFalse();
    }

    @Test
    public void givenEmptyExcludesWhenIsFileExcludedFromBackupThenReturnsFalse() throws IOException {
        Mockito.when(applicationConfig.getExcludedFileExtensions()).thenReturn(Collections.emptySet());
        Path file = createTestFile("somefile.dmp");

        service.initExcludedExtensions();
        assertThat(service.isFileExcludedFromBackup(file)).isFalse();
    }

    @Test
    public void givenAbsolutePathExcludeWhenFileMatchesThenItemIsNotScheduleForBackup() {

        Assert.fail();
    }

    @Test
    public void givenAbsolutePathExcludeWhenFileNotMatchesThenItemIsScheduleForBackup() {
        Assert.fail();

    }

    @Test
    public void givenFileNameExcludeWhenFileNameMatchesThenItemIsNotScheduledForBackup() {
        Assert.fail();

    }

    @Test
    public void givenFileNameExcludeWhenFileNotMatchesThenItemIsScheduledForBackup() {
        Assert.fail();

    }

}