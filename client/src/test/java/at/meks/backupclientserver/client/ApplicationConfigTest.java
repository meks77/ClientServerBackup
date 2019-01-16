package at.meks.backupclientserver.client;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import at.meks.validation.result.ValidationException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

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
import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.mockito.Mockito.when;

public class ApplicationConfigTest {

    private static final String DEFAULT_HOST_ENTRY = "server.host=theServerHostName";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private FileService fileService;

    private File configFile;

    @InjectMocks
    private ApplicationConfig config = new ApplicationConfig();


    @Before
    public void createConfigDirAndFile() throws IOException {
        Path applicationRoot = TestDirectoryProvider.createTempDirectory();
        configFile = Files.createFile(applicationRoot.resolve(".config")).toFile();
        when(fileService.getConfigFile()).thenReturn(configFile.toPath());
    }

    @After
    public void deleteConfigDirAndFiles() throws IOException {
        FileUtils.forceDeleteOnExit(configFile.getParentFile());
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
        writeLines(configFile, singleton(DEFAULT_HOST_ENTRY));
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
        writeLines(configFile, singleton(DEFAULT_HOST_ENTRY));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(startsWith("Configured directories for backup"));

        config.validate();
    }

    @Test
    public void givenFileWithNotExistingBackupsetWhenValidateThenExceptionIsThrown() throws IOException, ValidationException {
        writeLines(configFile, asList(DEFAULT_HOST_ENTRY, getBackupsetConfigEntryFor("C:/whatever", 0)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(startsWith("Configured directory C:\\whatever must exist"));

        config.validate();
    }

    @Test
    public void givenBackupsetIsAFileWhenValidateThenExceptionIsThrown() throws IOException, ValidationException {
        Path backupSetFile = TestDirectoryProvider.createTempDirectory().resolve("testFile.txt");
        Files.createFile(backupSetFile);
        writeLines(configFile, asList(DEFAULT_HOST_ENTRY,
                getBackupsetConfigEntryFor(backupSetFile.toString().replace("\\", "\\\\"), 0)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(startsWith(format("Path %s must be a directory", backupSetFile)));

        config.validate();
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
