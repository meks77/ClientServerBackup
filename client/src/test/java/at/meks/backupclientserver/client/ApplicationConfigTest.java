package at.meks.backupclientserver.client;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

public class ApplicationConfigTest {

    private File configFile;

    private ApplicationConfig config = new ApplicationConfig();

    @Before
    public void createConfigDirAndFile() throws IOException {
        Path appConfigUT = Files.createTempDirectory("appConfigUT");
        System.setProperty("user.home", appConfigUT.toString());
        File userDirectory = FileUtils.getUserDirectory();
        assertThat(userDirectory.getAbsolutePath()).isEqualTo(appConfigUT.toString());
        File configRoot = new File(userDirectory, ".ClientServerBackup");
        configFile = new File(configRoot, ".config");

        Files.deleteIfExists(configFile.toPath());

        Files.createDirectory(configRoot.toPath());
        Files.createFile(configFile.toPath());
    }

    @Test
    public void givenConfigFileWith1EntryWhenGetBackupedDirsReturnsThePathOfTheEntry() throws IOException {
        FileUtils.writeLines(configFile, Collections.singleton(getConfigEntryFor("C:\\\\dir1", 0)));
        assertThat(config.getBackupedDirs()).containsOnly(Paths.get("C:\\dir1"));
    }

    private String getConfigEntryFor(String path, int entryIndex) {
        return "backupset.dir" + entryIndex + "=" + path;
    }

    @Test
    public void givenConfigFileWith3EntriesWhenGetBackupedDirsReturnsPathsOfAllEntries() throws IOException {
        FileUtils.writeLines(configFile, Arrays.asList(getConfigEntryFor("C:\\\\dir1", 0),
                getConfigEntryFor("C:\\\\anotherDir\\\\dir2", 1),
                getConfigEntryFor("D:\\\\dir\\\\dir3", 2)));
        assertThat(config.getBackupedDirs()).containsOnly(Paths.get("C:\\dir1"), Paths.get("C:\\anotherDir\\dir2"),
                Paths.get("D:\\dir\\dir3"));
    }

    @Test(expected = ClientBackupException.class)
    public void givenNoConfigFileWhenGetBackupedDirsReturnsEmptyArray() throws IOException {
        Files.deleteIfExists(configFile.toPath());
        config.getBackupedDirs();
    }

    @Test
    public void givenEmptyConfigFileWhenGetBackupedDirsReturnsEmptyArray() {
        assertThat(config.getBackupedDirs()).isEmpty();
    }

    @Test
    public void testGetServerHostReturnsLocalhost() {
        assertThat(config.getServerHost()).isEqualTo("localhost");
    }

    @Test
    public void testGetServerPortReturns8080() {
        assertThat(config.getServerPort()).isEqualTo(8080);
    }
}
