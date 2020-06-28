package at.meks.backupclientserver.client;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    private static final String APPLICATION_DIR_NAME = ".ClientServerBackup";

    @Mock
    private ConfigFileInitializer configFileInitializer;

    @InjectMocks
    private FileService fileService = new FileService();

    @TempDir
    Path userHomePath;

    @BeforeEach
    public void initUserHome() {
        System.setProperty("user.home", userHomePath.toString());
    }

    @Test
    public void givenNoConfigFileWhenGetConfigFileThenNewFileIsReturned() {
        Path configFile = fileService.getConfigFile();
        assertThat(configFile).isNotNull();
        assertThat(configFile.toFile()).exists().isFile();
        assertThat(configFile.getParent()).isEqualTo(getApplicationRoot());
    }

    private Path getApplicationRoot() {
        return userHomePath.resolve(APPLICATION_DIR_NAME);
    }

    @Test
    public void givenNoConfigFileWhenGetConfigFileThenFileIsInitialized() {
        Path configFile = fileService.getConfigFile();
        verify(configFileInitializer).initializeConfigFile(configFile);
    }

    @Test
    public void givenExistingFileWhenGetConfigFileThenExistingFileIsReturned() throws IOException {
        Path configFile = Files.createFile(Files.createDirectory(getApplicationRoot()).resolve(".config"));
        String expectedFileContent = "this is the config file";
        FileUtils.write(configFile.toFile(), expectedFileContent, Charset.defaultCharset());

        Path result = fileService.getConfigFile();
        assertThat(result).isEqualTo(configFile);
        assertThat(configFile.toFile()).hasContent(expectedFileContent);
    }

    @Test
    public void givenExistingFileWhenGetConfigFileThenFileContentIsNotInitialized() throws IOException {
        Files.createFile(Files.createDirectory(getApplicationRoot()).resolve(".config"));
        fileService.getConfigFile();
        verifyNoInteractions(configFileInitializer);
    }

    @Test
    public void givenDirectoryWatchKeyFileExistsWhenCleanupDirectoriesMapFilesThenFileIsDeleted() throws IOException {
        Path applicationDir = Files.createDirectories(getApplicationRoot());
        Path expectedDeletedFile = Files.createFile(applicationDir.resolve("directoriesWatchKey93854098345098.dir"));

        fileService.cleanupDirectoriesMapFiles();

        assertThat(expectedDeletedFile.toFile()).doesNotExist();
    }

    @Test
    public void whenGetDirectoryWatchKeyFileThenNewEmptyFileIsReturned() {
        Path result = fileService.getDirectoriesMapFile();

        assertThat(result.toFile()).exists();
        assertThat(result.toFile().length()).isEqualTo(0L);
        assertThat(result.getParent()).isEqualTo(getApplicationRoot());
    }
}