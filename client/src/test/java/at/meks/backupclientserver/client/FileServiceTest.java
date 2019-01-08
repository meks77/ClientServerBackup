package at.meks.backupclientserver.client;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.fest.assertions.api.Assertions.assertThat;

public class FileServiceTest {

    private static final String APPLICATION_DIR_NAME = ".ClientServerBackup";

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
    public void givenExistingFileWhenGetConfigFileThenExistingFileIsReturned() throws IOException {
        Path configFile = Files.createFile(Files.createDirectory(getApplicationRoot()).resolve(".config"));
        String expectedFileContent = "this is the config file";
        FileUtils.write(configFile.toFile(), expectedFileContent, Charset.defaultCharset());

        Path result = fileService.getConfigFile();
        assertThat(result).isEqualTo(configFile);
        assertThat(configFile.toFile()).hasContent(expectedFileContent);

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