package at.meks.backupclientserver.client;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ConfigFileInitializerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private SystemService systemService;

    @InjectMocks
    private ConfigFileInitializer initializer = new ConfigFileInitializer();
    private Path tempDirectory;
    private Path configFile;

    @Before
    public void createTempDir() throws IOException {
        tempDirectory = TestDirectoryProvider.createTempDirectory();
        configFile = Files.createFile(tempDirectory.resolve("configFile.txt"));

    }

    @After
    public void deleteTempDir() throws IOException {
        FileUtils.forceDeleteOnExit(tempDirectory.toFile());
    }

    @Test
    public void givenOsWindowsWhenInitializeConfigFileThenWindowsAndAllOsExcludesAreAdded() {
        when(systemService.isOsWindows()).thenReturn(true);
        initializer.initializeConfigFile(configFile);
        assertThat(configFile.toFile()).usingCharset("iso-8859-1").hasContent(
                getExpectedFileContent("excludes.exclude0 = **/.idea/*tmp__",
                        "excludes.exclude1 = **/*.lock",
                        "excludes.exclude2 = C:/Users/*/AppData",
                        "excludes.exclude3 = C:/Users/*/.ClientServerBackup/directoriesWatchKey*.dir",
                        "excludes.exclude4 = C:/Users/*/NTUSER.DAT",
                        "excludes.exclude5 = C:/Users/*/ntuser.dat.LOG*"));
    }

    private String getExpectedFileContent(String...lines) {
        return Arrays.stream(lines).collect(Collectors.joining(System.lineSeparator()));
    }

    @Test
    public void givenOsLinuxWhenInitializeConfigFileThenLinuxAndAllOsExcludesAreAdded() {
        when(systemService.isOsLinux()).thenReturn(true);
        initializer.initializeConfigFile(configFile);
        assertThat(configFile.toFile()).usingCharset("iso-8859-1").hasContent(
                getExpectedFileContent("excludes.exclude0 = **/.idea/*tmp__",
                        "excludes.exclude1 = **/*.lock",
                        "excludes.exclude2 = /home/*/.ClientServerBackup/directoriesWatchKey*.dir"));
    }

    @Test
    public void givenNotWindowsAndNotLinuxWhenInitializeConfigFileThenOnlyAllOsExcludesAreAdded() {
        initializer.initializeConfigFile(configFile);
        assertThat(configFile.toFile()).usingCharset("iso-8859-1").hasContent(
                getExpectedFileContent("excludes.exclude0 = **/.idea/*tmp__",
                        "excludes.exclude1 = **/*.lock"));
    }

    @Test
    public void givenNotExistingFileWhenInitializeConfigFileThenClientBackupExceptionIsThrown() {
        expectedException.expect(ClientBackupException.class);
        expectedException.expectCause(CoreMatchers.instanceOf(NoSuchFileException.class));
        initializer.initializeConfigFile(tempDirectory.resolve("notExistingFile.txt"));
    }

}