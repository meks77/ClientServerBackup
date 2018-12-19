package at.meks.backupclientserver.backend.services.file;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.BackupConfiguration;
import at.meks.backupclientserver.backend.services.LockService;
import at.meks.backupclientserver.backend.services.ServerBackupException;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.junit.Before;
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
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DirectoryServiceTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    private static final String META_DATA_DIRECTORY_NAME = ".backupClientServer";
    private static final String BACKUPED_FILE_NAME = "backupedFile.txt";
    private static final String DIR_FOR_DELETE = "dirForDelete";
    private static final String DELETED_DIRS = "deletedDirs";
    private static final String CLIENT_BACKUP_SET_PATH = "/backup/set/path";

    private final DateTimeFormatter deletedDirNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss.SSS");

    @Rule
    public SpringMethodRule repeatRule = new SpringMethodRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BackupConfiguration configuration;

    @Mock
    private LockService lockService;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private DirectoryService service = new DirectoryService();

    private Path backupRootPath;

    @Before
    public void initService() {
        backupRootPath = TestDirectoryProvider.createTempDirectory();
        when(configuration.getApplicationRoot()).thenReturn(backupRootPath.toString());
        when(lockService.runWithLock(any(), any()))
                .thenAnswer(invocationOnMock -> ((Supplier<?>)invocationOnMock.getArgument(1)).get());
        when(clientRepository.getClient(any())).thenReturn(Optional.empty());
        when(clientRepository.createNewClient(any(), any()))
                .thenAnswer(invocation -> createClient(invocation.getArgument(0), invocation.getArgument(1)));
    }

    private Client createClient(String hostName, String directoryName1) {
        Client client = Client.builder().build();
        client.setName(hostName);
        client.setDirectoryName(directoryName1);
        client.setBackupSets(new LinkedList<>());
        return client;
    }

    //    @RepeatedTest(10) sadly junit 5 doesn't work with mockito :(
    @Test
    @Repeat(10)
    public void givenManyThreadsForSameHostAndBackupSetPathWhenGetBackupSetPathThenNoExceptionIsThrown()
            throws InterruptedException {
        String clientBackupSetPath = CLIENT_BACKUP_SET_PATH;
        ReentrantLock lock = new ReentrantLock();
        reset(lockService);
        when(lockService.getLockForPath(any())).thenReturn(lock);
        when(lockService.runWithLock(any(), any())).thenAnswer(invocationOnMock -> {
            ReentrantLock givenLock = invocationOnMock.getArgument(0);
            givenLock.lock();
            try {
                return ((Supplier<?>)invocationOnMock.getArgument(1)).get();
            } finally {
                givenLock.unlock();
            }
        });
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        String clientHostName = "utHostName";
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> service.getBackupSetPath(clientHostName, clientBackupSetPath));
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.MINUTES);
        assertThat(getExpectedPath(backupRootPath, clientBackupSetPath, clientHostName)).exists();
    }

    @Test
    public void givenHostNameAndBackupSetPathWhenGetBackupSetPathReturnsExpected() {
        String clientBackupSetPath = CLIENT_BACKUP_SET_PATH;
        String clientHostName = "utHostName";

        when(configuration.getApplicationRoot()).thenReturn(backupRootPath.toString());

        service.getBackupSetPath(clientHostName, clientBackupSetPath);
        assertThat(getExpectedPath(backupRootPath, clientBackupSetPath, clientHostName)).exists();
    }

    private Path getExpectedPath(Path tempDir, String clientBackupSetPath, String clientHostName) {
        return Paths.get(tempDir.toString(), "backups", md5Hex(clientHostName), md5Hex(clientBackupSetPath));
    }

    @Test
    public void whenGetMetadataDirectoryPathReturnsExcpectedPath() {
        Path result = service.getMetadataDirectoryPath(backupRootPath);
        assertThat(result).isEqualTo(Paths.get(backupRootPath.toString(), META_DATA_DIRECTORY_NAME));
    }

    @Test
    public void givenMetadataDirNotExistsWhenGetMetadataDirectoryPathThenDirIsCreated() {
        Path result = service.getMetadataDirectoryPath(backupRootPath);
        assertThat(result).exists();
    }

    @Test
    public void givenMetadataDirExistsWhenGetMetadataDirectoryPathThenNoExceptionIsThrown() throws IOException {
        Files.createDirectory(Paths.get(backupRootPath.toString(), META_DATA_DIRECTORY_NAME));
        service.getMetadataDirectoryPath(backupRootPath);
    }

    @Test
    public void givenNotExistingVersionsDirWhenGetVersionDirectoryThenVersionDirIsCreatedAndReturned() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path backupedFile = backupSetPath.resolve(BACKUPED_FILE_NAME);
        Path expectedVersionsDir = backupSetPath.resolve(META_DATA_DIRECTORY_NAME)
                .resolve(backupedFile.toFile().getName());

        Path versionDir = service.getFileVersionsDirectory(backupedFile);

        assertThat(versionDir).isEqualTo(expectedVersionsDir);
        assertThat(versionDir).isDirectory();
    }

    @Test
    public void givenExistingVersionsDirWhenGetVersionDirectoryThenVersionDirIsCreatedAndReturned() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path backupedFile = backupSetPath.resolve(BACKUPED_FILE_NAME);
        Path expectedVersionsDir = backupSetPath.resolve(META_DATA_DIRECTORY_NAME)
                .resolve(backupedFile.toFile().getName());
        Files.createDirectories(expectedVersionsDir);

        Path versionDir = service.getFileVersionsDirectory(backupedFile);

        assertThat(versionDir).isEqualTo(expectedVersionsDir);
        assertThat(versionDir).isDirectory();
    }

    @Test
    public void givenFileInSubDirWhenGetVersionDirectoryThenExpectedDirectoryIsReturned() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path backupedFile = backupSetPath.resolve("subDir").resolve(BACKUPED_FILE_NAME);
        Path expectedVersionsDir = backupedFile.getParent()
                .resolve(META_DATA_DIRECTORY_NAME)
                .resolve(backupedFile.toFile().getName());

        Path versionDir = service.getFileVersionsDirectory(backupedFile);

        assertThat(versionDir).isEqualTo(expectedVersionsDir);
        assertThat(versionDir).isDirectory();
    }

    @Test
    public void givenExistingDeletedDirsWhenGetDeletedVersionsDirReturnsExpected() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path dirToDelete = Files.createDirectories(backupSetPath.resolve(DIR_FOR_DELETE));
        Path expectedDeletedDirs = backupSetPath.resolve(META_DATA_DIRECTORY_NAME).resolve(DELETED_DIRS);

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
        Path dirToDelete = backupSetPath.resolve(DIR_FOR_DELETE);
        Path expectedDeletedDirs = backupSetPath.resolve(META_DATA_DIRECTORY_NAME).resolve(DELETED_DIRS);

        Path deletedDirsDirectory = service.getDirectoryForDeletedDir(dirToDelete);
        assertThat(deletedDirsDirectory).doesNotExist().hasFileName(DIR_FOR_DELETE);
        assertThat(deletedDirsDirectory.getParent()).exists().isDirectory().hasParent(expectedDeletedDirs);
    }

    @Test
    public void givenSubdirWhenGetDeletedVersionsDirThenCreatedDirIsReturned() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path dirToDelete = backupSetPath.resolve("subdir").resolve(DIR_FOR_DELETE);
        Path expectedDeletedDirs = backupSetPath.resolve("subdir").resolve(META_DATA_DIRECTORY_NAME).resolve(
                DELETED_DIRS);

        Path deletedDirsDirectory = service.getDirectoryForDeletedDir(dirToDelete);
        assertThat(deletedDirsDirectory).doesNotExist().hasFileName(DIR_FOR_DELETE);
        assertThat(deletedDirsDirectory.getParent()).exists().isDirectory().hasParent(expectedDeletedDirs);
    }

    @Test(expected = ServerBackupException.class)
    public void givenNoDirWhenGetApplicationRootDirectoryThenExceptionIsThrown() {
        when(configuration.getApplicationRoot()).thenReturn(null);
        service.getBackupSetPath("host", "path");
    }

    @Test
    public void givenNewClientWhenGetBackupSetPathThenNewClientIsCreated() {
        String hostName = "theNewHostname";
        when(clientRepository.getClient(hostName)).thenReturn(Optional.empty());
        when(clientRepository.createNewClient(eq(hostName), any()))
                .thenAnswer(invocation -> createClient(hostName, invocation.getArgument(1)));

        service.getBackupSetPath(hostName, "whatever");

        verify(clientRepository).createNewClient(eq(hostName), any());
    }

    @Test
    public void givenExistingClientWhenGetBackupSetPathThenNoClientIsCreated() throws IOException {
        String hostName = "theNewHostname";
        String expectedDirName ="dirForNewHost";
        Client client = createClient(hostName, expectedDirName);
        Files.createDirectories(this.backupRootPath.resolve(client.getDirectoryName()));
        when(clientRepository.getClient(hostName)).thenReturn(Optional.of(client));

        Path backupSetPath = service.getBackupSetPath(hostName, "whatever");

        assertThat(backupSetPath).hasParent(this.backupRootPath.resolve("backups").resolve(expectedDirName));
        verify(clientRepository, never()).createNewClient(any(), any());
    }
}
