package at.meks.backupclientserver.client;

import at.meks.validation.result.ValidationException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.writeLines;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationConfigTest {

    private static final String DEFAULT_HOST_ENTRY = "server.host=theServerHostName";

    @Mock
    private FileService fileService;

    private File configFile;

    @InjectMocks
    private ApplicationConfig config = new ApplicationConfig();

    @TempDir
    Path applicationRoot;

    @BeforeEach
    public void createConfigDirAndFile() throws IOException {
        configFile = Files.createFile(applicationRoot.resolve(".config")).toFile();
        when(fileService.getConfigFile()).thenReturn(configFile.toPath());
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

    @Test
    public void givenNoConfigFileWhenGetBackupedDirsReturnsEmptyArray() throws IOException {
        Files.deleteIfExists(configFile.toPath());
        assertThrows(ClientBackupException.class, () -> config.getBackupedDirs());

    }

    @Test
    public void givenEmptyConfigFileWhenGetBackupedDirsReturnsEmptyArray() {
        assertThat(config.getBackupedDirs()).isEmpty();
    }

    @Test
    public void givenFileWithServernameWhenGetServerHostThenReturnsExpectedHostName() throws IOException {
        writeLines(configFile, singleton(DEFAULT_HOST_ENTRY));
        assertThat(config.getServerHost()).isEqualTo("theServerHostName");
    }

    @Test
    public void testGetServerPortReturns8080() {
        assertThat(config.getServerPort()).isEqualTo(8080);
    }

    @Test
    public void givenFileWithoutHostNameWhenValidateThenExceptionIsThrown() throws IOException {
        writeLines(configFile, singletonList(getBackupsetConfigEntryFor("C:/whatever", 0)));

        assertThatThrownBy(() -> config.validate())
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Config property server.host");
    }

    @Test
    public void givenFileWithoutBackupsetWhenValidateThenExceptionIsThrown() throws IOException {
        writeLines(configFile, singleton(DEFAULT_HOST_ENTRY));

        assertThatThrownBy(() -> config.validate())
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Configured directories for backup");
    }

    @Test
    public void givenFileWithNotExistingBackupsetWhenValidateThenExceptionIsThrown() throws IOException {
        writeLines(configFile, asList(DEFAULT_HOST_ENTRY, getBackupsetConfigEntryFor("C:/whatever", 0)));

        assertThatThrownBy(() -> config.validate())
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Configured directory C:" + File.separator + "whatever must exist");
    }

    @Test
    public void givenBackupsetIsAFileWhenValidateThenExceptionIsThrown(@TempDir Path tempDir) throws IOException {
        Path backupSetFile = tempDir.resolve("testFile.txt");
        Files.createFile(backupSetFile);
        writeLines(configFile, asList(DEFAULT_HOST_ENTRY,
                getBackupsetConfigEntryFor(backupSetFile.toString().replace("\\", "\\\\"), 0)));

        assertThatThrownBy(() -> config.validate())
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith(format("Path %s must be a directory", backupSetFile));
    }

    @Test
    public void givenValidConfigWhenValidateThenNoExceptionIsThrown() throws IOException, ValidationException {
        writeLines(configFile, asList(DEFAULT_HOST_ENTRY,
                getBackupsetConfigEntryFor(FileUtils.getUserDirectoryPath().replace("\\", "\\\\"), 0)));

        config.validate();
    }

    @Test
    public void givenNoExcludesWhenGetPathExcludesForBackupThenEmptyListIsReturned() {
        List<String> result = config.getPathExcludesForBackup();
        assertThat(result).isEmpty();
    }

    @Test
    public void given2ExcludesWhenGetPathExcludesForBackupThen3ExcludesOfPropsAreReturned() throws IOException {
        writeLines(configFile, asList("excludes.exclude1 = exclude1", "excludes.exclude2 = exclude2"));
        List<String> result = config.getPathExcludesForBackup();
        assertThat(result).containsOnly("exclude1", "exclude2");
    }

    @Test
    public void givenNoFileExtensionEcludesWhenGetExcludedFileExtensionsThenEmptyArrayIsReturned() {
        Set<String> result = config.getExcludedFileExtensions();
        assertThat(result).isEmpty();
    }

    @Test
    public void givenOneFileExtensionEcludeWhenGetExcludedFileExtensionsThenArrayWithExpectedExtensionIsReturned() throws IOException {
        writeLines(configFile, singletonList("excludes.fileextensions = tmp"));
        Set<String> result = config.getExcludedFileExtensions();
        assertThat(result).containsOnly("tmp");
    }

    @Test
    public void givenMoreFileExtensionEcludesWhenGetExcludedFileExtensionsThenExpectedExtensionsAreReturned() throws IOException {
        writeLines(configFile, singletonList("excludes.fileextensions = tmp,lock,dmp"));
        Set<String> result = config.getExcludedFileExtensions();
        assertThat(result).containsOnly("tmp", "lock", "dmp");
    }

    @Test
    public void givenFileExtensionWithSpaceNearbyKommaWhenGetExcludedFileExtensionsThenExpectedExtensionsAreReturned() throws IOException {
        writeLines(configFile, singletonList("excludes.fileextensions = tmp , lock , dmp"));
        Set<String> result = config.getExcludedFileExtensions();
        assertThat(result).containsOnly("tmp", "lock", "dmp");
    }

    @Test
    public void givenExtensionsWhereOneEntryIsJustASpaceWhenGetExcludedFileExtensionsThenOtherExtensionsAreReturned() throws IOException {
        writeLines(configFile, singletonList("excludes.fileextensions = tmp, ,dmp"));
        Set<String> result = config.getExcludedFileExtensions();
        assertThat(result).containsOnly("tmp", "dmp");
    }

    @Test
    public void givenExtensionsWhereOneEntryIsEmptyWhenGetExcludedFileExtensionsThenOtherExtensionsAreReturned() throws IOException {
        writeLines(configFile, singletonList("excludes.fileextensions = tmp,,dmp"));
        Set<String> result = config.getExcludedFileExtensions();
        assertThat(result).containsOnly("tmp", "dmp");
    }

    @Test
    public void givenNoExcludeWhenGetExcludesReturnsEmptySet() {
        Set<String> excludes = config.getExcludes();
        assertThat(excludes).isNotNull().isEmpty();
    }

    @Test
    public void givenOneExcludeWhenGetExclucesReturnsThisExclude() throws IOException {
        writeLines(configFile, singletonList("excludes.exclude000=whatever"));
        Set<String> excludes = config.getExcludes();
        assertThat(excludes).containsOnly("whatever");
    }

    @Test
    public void givenMoreExcludesWhenGetExcludesReturnsAllExcludes() throws IOException {
        writeLines(configFile, asList("excludes.exclude000=whatever", "excludes.exclude1=whenever", "excludes.exclude02=wherever"));
        Set<String> excludes = config.getExcludes();
        assertThat(excludes).containsOnly("whatever", "whenever", "wherever");
    }

    @Test
    public void givenWrongExcludesWhenGetExcludesThenReturnsEmptySet() throws IOException {
        writeLines(configFile, asList("excludes=whatever", "exclude1=whenever", "excludesexclude02=wherever"));
        Set<String> excludes = config.getExcludes();
        assertThat(excludes).isNotNull().isEmpty();
    }

}
