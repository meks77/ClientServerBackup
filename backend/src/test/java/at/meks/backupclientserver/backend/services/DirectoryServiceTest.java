package at.meks.backupclientserver.backend.services;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class DirectoryServiceTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    private final DateTimeFormatter deletedDirNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss" +
            ".SSS");

    @Rule
    public SpringMethodRule repeatRule = new SpringMethodRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BackupConfiguration configuration;

    @InjectMocks
    private DirectoryService service;

//    @RepeatedTest(10) sadly junit 5 doesn't work with mockito :(
    @Test
    @Repeat(10)
    public void givenManyThreadsForSameHostAndBackupSetPathWhenGetBackupSetPathThenNoExceptionIsThrown() throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("utThreading");
        String clientBackupSetPath = "C:\\backup\\set\\path";

        when(configuration.getApplicationRoot()).thenReturn(tempDir.toString());

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        String clientHostName = "utHostName";
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> service.getBackupSetPath(clientHostName, clientBackupSetPath));
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.MINUTES);
        assertThat(getExpectedPath(tempDir, clientBackupSetPath, clientHostName)).exists();
    }

    @Test
    public void givenHostNameAndBackupSetPathWhenGetBackupSetPathReturnsExpected() {
        Path tempDir = TestDirectoryProvider.createTempDirectory();
        String clientBackupSetPath = "C:\\backup\\set\\path";
        String clientHostName = "utHostName";

        when(configuration.getApplicationRoot()).thenReturn(tempDir.toString());

        service.getBackupSetPath(clientHostName, clientBackupSetPath);
        assertThat(getExpectedPath(tempDir, clientBackupSetPath, clientHostName)).exists();
    }

    private Path getExpectedPath(Path tempDir, String clientBackupSetPath, String clientHostName) {
        return Paths.get(tempDir.toString(), "backups", md5Hex(clientHostName), md5Hex(clientBackupSetPath));
    }

    @Test
    public void whenGetMetadataDirectoryPathReturnsExcpectedPath() {
        Path tempRootPath = TestDirectoryProvider.createTempDirectory();
        Path result = service.getMetadataDirectoryPath(tempRootPath);
        assertThat(result).isEqualTo(Paths.get(tempRootPath.toString(), ".backupClientServer"));
    }

    @Test
    public void givenMetadataDirNotExistsWhenGetMetadataDirectoryPathThenDirIsCreated() {
        Path tempRootPath = TestDirectoryProvider.createTempDirectory();
        Path result = service.getMetadataDirectoryPath(tempRootPath);
        assertThat(result).exists();
    }

    @Test
    public void givenMetadataDirExistsWhenGetMetadataDirectoryPathThenNoExceptionIsThrown() throws IOException {
        Path tempRootPath = TestDirectoryProvider.createTempDirectory();
        Files.createDirectory(Paths.get(tempRootPath.toString(), ".backupClientServer"));
        service.getMetadataDirectoryPath(tempRootPath);
    }

    @Test
    public void givenNotExistingVersionsDirWhenGetVersionDirectoryThenVersionDirIsCreatedAndReturned() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path backupedFile = backupSetPath.resolve("backupedFile.txt");
        Path expectedVersionsDir = backupSetPath.resolve(".backupClientServer")
                .resolve(backupedFile.toFile().getName());

        Path versionDir = service.getFileVersionsDirectory(backupedFile);

        assertThat(versionDir).isEqualTo(expectedVersionsDir);
        assertThat(versionDir).isDirectory();
    }

    @Test
    public void givenExistingVersionsDirWhenGetVersionDirectoryThenVersionDirIsCreatedAndReturned() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path backupedFile = backupSetPath.resolve("backupedFile.txt");
        Path expectedVersionsDir = backupSetPath.resolve(".backupClientServer")
                .resolve(backupedFile.toFile().getName());
        Files.createDirectories(expectedVersionsDir);

        Path versionDir = service.getFileVersionsDirectory(backupedFile);

        assertThat(versionDir).isEqualTo(expectedVersionsDir);
        assertThat(versionDir).isDirectory();
    }

    @Test
    public void givenFileInSubDirWhenGetVersionDirectoryThenExpectedDirectoryIsReturned() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path backupedFile = backupSetPath.resolve("subDir").resolve("backupedFile.txt");
        Path expectedVersionsDir = backupedFile.getParent()
                .resolve(".backupClientServer")
                .resolve(backupedFile.toFile().getName());

        Path versionDir = service.getFileVersionsDirectory(backupedFile);

        assertThat(versionDir).isEqualTo(expectedVersionsDir);
        assertThat(versionDir).isDirectory();
    }

    @Test
    public void givenExistingDeletedDirsWhenGetDeletedVersionsDirReturnsExpected() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path dirToDelete = Files.createDirectories(backupSetPath.resolve("dirForDelete"));
        Path expectedDeletedDirs = backupSetPath.resolve(".backupClientServer").resolve("deletedDirs");

        LocalDateTime beforeExecution = LocalDateTime.now();
        Path versionedDeleteDir = service.getDirectoryForDeletedDir(dirToDelete);

        assertThat(versionedDeleteDir).doesNotExist();
        assertThat(versionedDeleteDir.getParent()).exists().isDirectory();
        assertThat(versionedDeleteDir.getParent().getParent()).isEqualTo(expectedDeletedDirs).exists().isDirectory();

        TemporalAccessor dateOfDeletedDir = deletedDirNameFormatter.parse(versionedDeleteDir.getParent().toFile().getName());
        assertThat(LocalDateTime.from(dateOfDeletedDir))
                .isAfterOrEqualTo(beforeExecution)
                .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void givenNotExistingDeletedDirsWhenGetDeletedVersionsDirThenCreatedDirIsReturned() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path dirToDelete = backupSetPath.resolve("dirForDelete");
        Path expectedDeletedDirs = backupSetPath.resolve(".backupClientServer").resolve("deletedDirs");

        Path deletedDirsDirectory = service.getDirectoryForDeletedDir(dirToDelete);
        assertThat(deletedDirsDirectory).doesNotExist().hasFileName("dirForDelete");
        assertThat(deletedDirsDirectory.getParent()).exists().isDirectory().hasParent(expectedDeletedDirs);
    }

    @Test
    public void givenSubdirWhenGetDeletedVersionsDirThenCreatedDirIsReturned() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path dirToDelete = backupSetPath.resolve("subdir").resolve("dirForDelete");
        Path expectedDeletedDirs = backupSetPath.resolve("subdir").resolve(".backupClientServer").resolve("deletedDirs");

        Path deletedDirsDirectory = service.getDirectoryForDeletedDir(dirToDelete);
        assertThat(deletedDirsDirectory).doesNotExist().hasFileName("dirForDelete");
        assertThat(deletedDirsDirectory.getParent()).exists().isDirectory().hasParent(expectedDeletedDirs);
    }

    @Test
    public void givenNoDirWhenGetApplicationRootDirectoryThenExceptionIsThrown() {
        assertThrows(ServerBackupException.class, () -> service.getBackupSetPath("host", "path"));
    }

}
