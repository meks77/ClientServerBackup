package at.meks.clientserverbackup.client;

import org.apache.commons.io.FileUtils;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

public class ApplicationConfigTest {

    private File configFile;
    private File configRoot;

    @Rule
    public ExpectedException  expectedException = ExpectedException.none();

    private ApplicationConfig config = new ApplicationConfig();

    @Before
    public void createConfigDirAndFile() throws IOException {
        Path appConfigUT = Files.createTempDirectory("appConfigUT");
        System.setProperty("user.home", appConfigUT.toString());
        File userDirectory = FileUtils.getUserDirectory();
        assertThat(userDirectory.getAbsolutePath()).isEqualTo(appConfigUT.toString());
        configRoot = new File(userDirectory, ".ClientServerBackup");
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

    @Test
    public void givenNoConfigFileWhenGetBackupedDirsReturnsEmptyArray() throws IOException {
        Files.deleteIfExists(configFile.toPath());
        expectedException.expect(ClientBackupException.class);
        expectedException.expectCause(IsInstanceOf.instanceOf(FileNotFoundException.class));
        config.getBackupedDirs();
    }

    @Test
    public void givenEmptyConfigFileWhenGetBackupedDirsReturnsEmptyArray() {
        assertThat(config.getBackupedDirs()).isEmpty();
    }
}
