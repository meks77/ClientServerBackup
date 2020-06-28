package at.meks.backupclientserver.client;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConfigFileInitializerTest {

    @Mock
    private SystemService systemService;

    @InjectMocks
    private ConfigFileInitializer initializer = new ConfigFileInitializer();

    @TempDir
    Path tempDirectory;

    private Path configFile;

    @BeforeEach
    public void createTempDir() throws IOException {
        configFile = Files.createFile(tempDirectory.resolve("configFile.txt"));

    }

    @AfterEach
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
                        "excludes.exclude2 = /home/*/.ClientServerBackup/directoriesWatchKey*.dir",
                        "excludes.exclude3 = /home/*/.cache",
                        "excludes.exclude4 = /home/*/.local",
                        "excludes.exclude5 = /home/*/.config",
                        "excludes.exclude6 = /home/*/.mozilla/firefox"));
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
        assertThatThrownBy(() -> initializer.initializeConfigFile(tempDirectory.resolve("notExistingFile.txt")))
            .isInstanceOf(ClientBackupException.class)
            .hasCauseInstanceOf(NoSuchFileException.class);
    }

}