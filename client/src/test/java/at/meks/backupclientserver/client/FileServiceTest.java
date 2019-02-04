package at.meks.backupclientserver.client;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class FileServiceTest {

    private static final String APPLICATION_DIR_NAME = ".ClientServerBackup";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ConfigFileInitializer configFileInitializer;

    @InjectMocks
    private FileService fileService = new FileService();

    private Path userHomePath;

    @Before
    public void initUserHome() {
        userHomePath = TestDirectoryProvider.createTempDirectory();
        System.setProperty("user.home", userHomePath.toString());
    }

    @After
    public void deleteTempDir() throws IOException {
        FileUtils.forceDeleteOnExit(userHomePath.toFile());
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
        verifyZeroInteractions(configFileInitializer);
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