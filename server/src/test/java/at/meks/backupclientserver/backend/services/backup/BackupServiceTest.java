package at.meks.backupclientserver.backend.services.backup;

import at.meks.backupclientserver.backend.services.ClientService;
import at.meks.backupclientserver.backend.services.ServerBackupException;
import at.meks.backupclientserver.backend.services.file.DirectoryService;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BackupServiceTest {

    private static final String BACKUP_SET_PATH = "backupSetPath";
    private static final String HOST_NAME = "hostName";
    private static final String BACKUPED_FILE_TXT = "backupedFile.txt";
    private static final String BACKUP_SET = "backupSet";
    private static final String BACKUP_CLIENT_SERVER = ".backupClientServer";
    private static final String UPLOADED_FILE_NAME = "uploadedFile.txt";
    private final DateTimeFormatter versionedFileNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss" +
            ".SSS");

    @Mock
    private DirectoryService directoryService;

    @Mock
    private MetaDataService metaDataService;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private BackupService service = new BackupService();

    @Test
    public void givenFileWhenBackupThenFileIsSavedToExpectedDirectory(@TempDir Path uploadDir) throws IOException {
        final Path uploadedFile = uploadDir.resolve(UPLOADED_FILE_NAME);
        final String expectedContent = "uploaded file context";
        Files.write(uploadedFile, expectedContent.getBytes());
        String hostName = "utHostName";
        String clientBackupSetPath = "path\\to\\backup\\file";
        Path tempDir = Files.createTempDirectory("utBsT");
        Path backupSetTargetPath = Paths.get(tempDir.toString(), BACKUP_SET_PATH);
        String fileNameOfBackedupFile = "backedUpFilename.xpktd";
        Path expectedTarget = Paths.get(backupSetTargetPath.toString(), "expected", "target",
                fileNameOfBackedupFile);

        when(directoryService.getBackupSetPath(hostName, clientBackupSetPath)).thenReturn(backupSetTargetPath);
        FileInputArgs fileInputArgs = createFileInputArgs(hostName, clientBackupSetPath,
                new String[]{"expected", "target"}, fileNameOfBackedupFile);
        service.backup(uploadedFile, fileInputArgs);

        assertThat(expectedTarget).hasContent(expectedContent);
    }

    private FileInputArgs createFileInputArgs(String hostName, String clientBackupSetPath, String[] relativePath,
            String fileNameOfBackedupFile) {
        return FileInputArgs.aFileInputArgs().hostName(hostName)
                .backupedPath(clientBackupSetPath)
                .relativePath(relativePath)
                .fileName(fileNameOfBackedupFile).build();
    }

    @Test
    public void givenExceptionWhenBackupThenServerBackupExceptionIsThrown(@TempDir Path tempDir) {
        doThrow(new NullPointerException("ut expktd exc")).when(directoryService).getBackupSetPath(any(), any());
        FileInputArgs fileInputArgs = createFileInputArgs("myHostName", "C:\\f1\\f2", new String[]{"a", "b", "c"},
                "whatever");

        assertThatThrownBy(() -> service.backup(Files.createFile(tempDir.resolve(UPLOADED_FILE_NAME)), fileInputArgs))
                .isInstanceOf(ServerBackupException.class);
    }

    @Test
    public void whenIsFileUp2dateThenMetaDataServiceIsInvokedWithExpectedArgs() {
        String hostName = "theHostName";
        String backupedPath = "theBackupedPath";
        String[] relativePath = new String[] {"theRelativePath"};
        String fileName = "theFileName";
        String md5Checksum = "theMd5Checksum";

        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        File backupedFile = Paths.get(backupSetPath.toString(), relativePath).resolve(fileName).toFile();

        when(directoryService.getBackupSetPath(hostName, backupedPath)).thenReturn(backupSetPath);
        service.isFileUpToDate(createFileInputArgs(hostName, backupedPath, relativePath, fileName), md5Checksum);

        verify(metaDataService).isMd5Equal(backupedFile, md5Checksum);
    }

    @Test
    public void givenMd5EqualsWhenIsFileUp2dateReturnsTrue() {
        when(directoryService.getBackupSetPath(any(), any())).thenReturn(TestDirectoryProvider.createTempDirectory());
        when(metaDataService.isMd5Equal(any(), any())).thenReturn(true);

        boolean result = service.isFileUpToDate(
                createFileInputArgs(HOST_NAME, "backedupPath", new String[]{"relativePath"}, "fileName"),
                "md5Checksum");
        assertThat(result).isTrue();
    }

    @Test
    public void givenMd5NotEqualsWhenIsFileUp2dateReturnsTrue() {
        when(directoryService.getBackupSetPath(any(), any())).thenReturn(TestDirectoryProvider.createTempDirectory());
        when(metaDataService.isMd5Equal(any(), any())).thenReturn(false);

        boolean result = service.isFileUpToDate(
                createFileInputArgs(HOST_NAME, "backedupPath", new String[]{"relativePath"}, "fileName"),
                "md5Checksum");
        assertThat(result).isFalse();
    }

    @Test
    public void givenFirstVersionOfFileWhenBackupThenNoFileIsCreatedInVersionsDir(@TempDir Path tempDir) throws IOException {
        Path testRootDir = Files.createDirectory(tempDir.resolve("rootDir"));
        Path backupSetPath = testRootDir.resolve(BACKUP_SET_PATH);
        Path versionsDir = backupSetPath.resolve(".versions");
        Path fileToBackup = testRootDir.resolve("fileForBackup.txt");
        Files.createFile(fileToBackup);
        final Path uploadedFile = Files.createFile(tempDir.resolve("uplodaedFile.txt"));

        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);

        service.backup(uploadedFile,
                createFileInputArgs(HOST_NAME, BACKUP_SET_PATH, new String[0], fileToBackup.toFile().getName()));

        assertThat(versionsDir).doesNotExist();
    }

    @Test
    public void givenSecondVersionOfFileWhenBackupThenTheOldFileIsMovedToVersionsDir(@TempDir Path tempDir) throws IOException {
        Path testRootDir = Files.createDirectory(tempDir.resolve("rootDir"));
        Path backupSetPath = testRootDir.resolve(BACKUP_SET_PATH);
        Files.createDirectories(backupSetPath);
        Path versionsDir = backupSetPath.resolve(".versions");
        Files.createDirectories(versionsDir);
        Path fileToBackup = testRootDir.resolve("fileForBackup.txt");
        Files.createFile(fileToBackup);
        Path backupTargetFile = backupSetPath.resolve(fileToBackup.toFile().getName());
        Files.createFile(backupTargetFile);

        final Path uploadedFile = Files.createFile(tempDir.resolve("uplodaedFile.txt"));

        when(directoryService.getFileVersionsDirectory(backupTargetFile)).thenReturn(versionsDir);
        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);

        LocalDateTime timeStampBeforeBackup = LocalDateTime.now();

        service.backup(uploadedFile,
                createFileInputArgs(HOST_NAME, BACKUP_SET_PATH, new String[0], fileToBackup.toFile().getName()));

        assertThat(versionsDir).exists().isDirectory();
        assertThat(versionsDir.toFile().list()).hasSize(1);
        @SuppressWarnings("ConstantConditions") String foundVersionFile = versionsDir.toFile().listFiles()[0].getName();
        TemporalAccessor timestampFromFilename = versionedFileNameFormatter.parse(foundVersionFile);
        assertThat(LocalDateTime.from(timestampFromFilename)).isAfterOrEqualTo(timeStampBeforeBackup);
    }

    @Test
    public void givenNotBackupedPathWhenDeleteThenNothingIsDone() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path backupedFile = backupSetPath.resolve(BACKUPED_FILE_TXT);

        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);

        service.delete(createFileInputArgs(HOST_NAME, BACKUP_SET, new String[0], backupedFile.toFile().getName()));

        verify(directoryService).getBackupSetPath(HOST_NAME, BACKUP_SET);
        verifyNoMoreInteractions(directoryService, metaDataService);
    }

    @Test
    public void givenFileWhenDeleteThenFileIsMovedToVersionsDir() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path backupedFile = Files.createFile(backupSetPath.resolve(BACKUPED_FILE_TXT));

        Path versionsDir = Files.createDirectories(backupSetPath.resolve(BACKUP_CLIENT_SERVER).resolve("backupFile.txt"));

        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);
        when(directoryService.getFileVersionsDirectory(backupedFile)).thenReturn(versionsDir);

        LocalDateTime timestampBeforeDelete = LocalDateTime.now();

        service.delete(createFileInputArgs(HOST_NAME, BACKUP_SET, new String[0], backupedFile.toFile().getName()));

        assertThat(backupedFile).doesNotExist();
        assertThat(versionsDir.toFile().list()).hasSize(1);
        @SuppressWarnings("ConstantConditions") String versionedFilename = versionsDir.toFile().list()[0];
        assertThat(versionedFilename).endsWith("-deleted");
        String versiondFileNameWithoutSuffix = versionedFilename.substring(0, versionedFilename.indexOf("-deleted"));
        TemporalAccessor parsedFileName = versionedFileNameFormatter.parse(versiondFileNameWithoutSuffix);
        assertThat(LocalDateTime.from(parsedFileName)).isAfterOrEqualTo(timestampBeforeDelete);
    }

    @Test
    public void givenDirectoryWhenDeleteThenDirectoryIsMovedToDeletedDirs() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path expectedTargetDir = Files.createDirectories(backupSetPath.resolve(BACKUP_CLIENT_SERVER)).resolve("expectedTargetDirOfDeletedDir");

        Path dirForDelete = backupSetPath.resolve("dirForDelete");
        Path subdirOfDeletedFolder = Files.createDirectories(dirForDelete.resolve("deletedSubDir"));
        Files.createFile(subdirOfDeletedFolder.resolve("fileInSubDir.txt"));
        Files.createFile(dirForDelete.resolve("file1.txt"));
        Files.createFile(dirForDelete.resolve("file2.txt"));
        Files.createFile(dirForDelete.resolve("file3.txt"));

        String[] expectedChilds = dirForDelete.toFile().list();

        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);
        when(directoryService.getDirectoryForDeletedDir(dirForDelete)).thenReturn(expectedTargetDir);

        service.delete(createFileInputArgs("host", backupSetPath.toString(), new String[0], dirForDelete.toFile().getName()));

        assertThatDirectoryWasMOvedToDeletedDirs(dirForDelete, expectedChilds, expectedTargetDir);
    }

    private void assertThatDirectoryWasMOvedToDeletedDirs(Path dirForDelete, String[] expectedChilds, Path expectedTargetDirOfDeleted) {
        assertThat(dirForDelete).doesNotExist();

        assertThat(expectedTargetDirOfDeleted).exists().isDirectory();
        assertThat(expectedTargetDirOfDeleted.toFile().list()).isEqualTo(expectedChilds);
    }

    @Test
    public void givenDirectoryInRelativePathWhenDeleteThenDirectoriesInEachSubFolderAreMovedToDeletedDirs() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path dirForDelete = Files.createDirectories(backupSetPath.resolve("folder1").resolve("dirForDelete"));
        Path subdirOfDeletedFolder = Files.createDirectories(dirForDelete.resolve("deletedSubDir"));

        Path expectedTargetDir = Files.createDirectories(
                backupSetPath.resolve(BACKUP_CLIENT_SERVER)
                        .resolve("currentDate"))
                .resolve(dirForDelete.toFile().getName());

        Files.createFile(subdirOfDeletedFolder.resolve("fileInSubDir.txt"));
        Files.createFile(dirForDelete.resolve("file1.txt"));
        Files.createFile(dirForDelete.resolve("file2.txt"));
        Files.createFile(dirForDelete.resolve("file3.txt"));

        String[] expectedChilds = dirForDelete.toFile().list();

        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);
        when(directoryService.getDirectoryForDeletedDir(dirForDelete)).thenReturn(expectedTargetDir);

        service.delete(createFileInputArgs("host", backupSetPath.toString(), new String[] {"folder1"},
                dirForDelete.toFile().getName()));

        assertThatDirectoryWasMOvedToDeletedDirs(dirForDelete, expectedChilds, expectedTargetDir);
    }

    @Test
    public void whenBackupThenClientsLastUpdateTimeIsPersisted(@TempDir Path tempDir) throws IOException {
        Path backupSetPath = Files.createDirectory(tempDir.resolve(BACKUP_SET_PATH));
        String hostName = "theUtHostName";
        final Path uploadedFile = Files.createFile(tempDir.resolve(UPLOADED_FILE_NAME));

        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);

        service.backup(uploadedFile, createFileInputArgs(hostName, backupSetPath.toString(), new String[0],
                BACKUPED_FILE_TXT));

        verify(clientService).updateLastBackupTimestamp(hostName);
    }
}
