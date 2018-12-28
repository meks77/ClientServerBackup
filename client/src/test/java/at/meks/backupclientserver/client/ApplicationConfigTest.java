package at.meks.backupclientserver.client;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import at.meks.validation.result.ValidationException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.writeLines;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;

public class ApplicationConfigTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private File configFile;

    private ApplicationConfig config = new ApplicationConfig();

    private Path userHome;

    @Before
    public void createConfigDirAndFile() throws IOException {
        userHome = TestDirectoryProvider.createTempDirectory();
        System.setProperty("user.home", userHome.toString());

        File userDirectory = FileUtils.getUserDirectory();
        assertThat(userDirectory.getAbsolutePath()).isEqualTo(userHome.toString());

        File configRoot = new File(userDirectory, ".ClientServerBackup");
        configFile = new File(configRoot, ".config");
        Files.createDirectory(configRoot.toPath());
        Files.createFile(configFile.toPath());
    }

    @Test
    public void givenConfigFileWith1EntryWhenGetBackupedDirsReturnsThePathOfTheEntry() throws IOException {
        writeLines(configFile, singleton(getBackupsetConfigEntryFor("C:\\\\dir1", 0)));
        assertThat(config.getBackupedDirs()).containsOnly(Paths.get("C:\\dir1"));
    }

    private String getBackupsetConfigEntryFor(String path, int entryIndex) {
        return "backupset.dir" + entryIndex + "=" + path;
    }

    @Test
    public void givenConfigFileWith3EntriesWhenGetBackupedDirsReturnsPathsOfAllEntries() throws IOException {
        writeLines(configFile, asList(getBackupsetConfigEntryFor("C:\\\\dir1", 0),
                getBackupsetConfigEntryFor("C:\\\\anotherDir\\\\dir2", 1),
                getBackupsetConfigEntryFor("D:\\\\dir\\\\dir3", 2)));
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
    public void givenFileWithServernameWhenGetServerHostThenReturnsExpectedHostName() throws IOException {
        writeLines(configFile, singleton("server.host=theServerHostName"));
        assertThat(config.getServerHost()).isEqualTo("theServerHostName");
    }

    @Test
    public void testGetServerPortReturns8080() {
        assertThat(config.getServerPort()).isEqualTo(8080);
    }

    @Test
    public void givenFileWithoutHostNameWhenValidateThenExceptionIsThrown() throws IOException, ValidationException {
        writeLines(configFile, singletonList(getBackupsetConfigEntryFor("C:/whatever", 0)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(startsWith("Config property server.host"));

        config.validate();
    }

    @Test
    public void givenFileWithoutBackupsetWhenValidateThenExceptionIsThrown() throws IOException, ValidationException {
        writeLines(configFile, singleton("server.host=theServerHostName"));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(startsWith("Configured directories for backup"));

        config.validate();
    }

    @Test
    public void givenFileWithNotExistingBackupsetWhenValidateThenExceptionIsThrown() throws IOException, ValidationException {
        writeLines(configFile, asList("server.host=theServerHostName", getBackupsetConfigEntryFor("C:/whatever", 0)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(startsWith("Configured directory C:\\whatever must exist"));

        config.validate();
    }

    @Test
    public void givenBackupsetIsAFileWhenValidateThenExceptionIsThrown() throws IOException, ValidationException {
        Path backupSetFile = userHome.resolve("testFile.txt");
        Files.createFile(backupSetFile);
        writeLines(configFile, asList("server.host=theServerHostName",
                getBackupsetConfigEntryFor(backupSetFile.toString().replace("\\", "\\\\"), 0)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(startsWith(format("Path %s must be a directory", backupSetFile)));

        config.validate();
    }

}
