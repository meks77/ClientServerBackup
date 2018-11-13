package at.meks.backupclientserver.backend.services;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BackupServiceTest {

    @Mock
    private DirectoryService directoryService;

    @Mock
    private MetaDataService metaDataService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private BackupService service = new BackupService();

    @Test
    public void givenFileWhenBackupThenFileIsSavedToExpectedDirectory() throws IOException {
        String hostName = "utHostName";
        String clientBackupSetPath = "path\\to\\backup\\file";
        Path tempDir = Files.createTempDirectory("utBsT");
        Path backupSetTargetPath = Paths.get(tempDir.toString(), "backupSetPath");
        String fileNameOfBackedupFile = "backedUpFilename.xpktd";
        File expectedTarget = Paths.get(backupSetTargetPath.toString(), "expected", "target",
                fileNameOfBackedupFile).toFile();

        when(directoryService.getBackupSetPath(hostName, clientBackupSetPath)).thenReturn(backupSetTargetPath);

        service.backup(multipartFile, hostName, clientBackupSetPath, new String[]{"expected", "target"},
                fileNameOfBackedupFile);

        verify(multipartFile).transferTo(expectedTarget);
    }

    @Test(expected = ServerBackupException.class)
    public void givenIoExceptionWhenBackupThenServerBackupExceptionIsThrown() throws IOException {
        doThrow(new IOException("ut expktd exc")).when(multipartFile).transferTo(any());
        when(directoryService.getBackupSetPath(any(), any())).thenReturn(Files.createTempDirectory("utThrw"));
        service.backup(multipartFile, "myHostName", "C:\\f1\\f2", new String[]{"a", "b", "c"}, "whatever");
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
        service.isFileUpToDate(hostName, backupedPath, relativePath, fileName, md5Checksum);

        verify(metaDataService).isMd5Equal(backupedFile, md5Checksum);
    }

    @Test
    public void givenMd5EqualsWhenIsFileUp2dateReturnsTrue() {
        when(directoryService.getBackupSetPath(any(), any())).thenReturn(TestDirectoryProvider.createTempDirectory());
        when(metaDataService.isMd5Equal(any(), any())).thenReturn(true);

        boolean result = service.isFileUpToDate("hostName", "backedupPath", new String[]{"relativePath"}, "fileName",
                "md5Checksum");
        assertThat(result).isTrue();
    }

    @Test
    public void givenMd5NotEqualsWhenIsFileUp2dateReturnsTrue() {
        when(directoryService.getBackupSetPath(any(), any())).thenReturn(TestDirectoryProvider.createTempDirectory());
        when(metaDataService.isMd5Equal(any(), any())).thenReturn(false);

        boolean result = service.isFileUpToDate("hostName", "backedupPath", new String[]{"relativePath"}, "fileName",
                "md5Checksum");
        assertThat(result).isFalse();
    }

    @Test
    public void givenFirstVersionOfFileWhenBackupThenNoFileIsCreatedInVersionsDir() throws IOException {
        Path testRootDir = TestDirectoryProvider.createTempDirectory();
        Path backupSetPath = testRootDir.resolve("backupSetPath");
        Path versionsDir = backupSetPath.resolve(".versions");
        Path fileToBackup = testRootDir.resolve("fileForBackup.txt");
        Files.createFile(fileToBackup);

        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);

        service.backup(multipartFile, "hostName", "backupSetPath", new String[0], fileToBackup.toFile().getName());

        assertThat(versionsDir).doesNotExist();
    }

    @Test
    public void givenSecondVersionOfFileWhenBackupThenTheOldFileIsMovedToVersionsDir() throws IOException {
        Path testRootDir = TestDirectoryProvider.createTempDirectory();
        Path backupSetPath = testRootDir.resolve("backupSetPath");
        Files.createDirectories(backupSetPath);
        Path versionsDir = backupSetPath.resolve(".versions");
        Files.createDirectories(versionsDir);
        Path fileToBackup = testRootDir.resolve("fileForBackup.txt");
        Files.createFile(fileToBackup);
        Path backupTargetFile = backupSetPath.resolve(fileToBackup.toFile().getName());
        Files.createFile(backupTargetFile);

        when(directoryService.getFileVersionsDirectory(backupTargetFile)).thenReturn(versionsDir);
        when(directoryService.getBackupSetPath(any(), any())).thenReturn(backupSetPath);

        LocalDateTime timeStampBeforeBackup = LocalDateTime.now();

        service.backup(multipartFile, "hostName", "backupSetPath", new String[0], fileToBackup.toFile().getName());

        assertThat(versionsDir).exists().isDirectory();
        assertThat(versionsDir.toFile().list()).hasSize(1);
        @SuppressWarnings("ConstantConditions") String foundVersionFile = versionsDir.toFile().listFiles()[0].getName();
        TemporalAccessor timestampFromFilename = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss.SSS").parse(foundVersionFile);
        assertThat(LocalDateTime.from(timestampFromFilename)).isAfterOrEqualTo(timeStampBeforeBackup);
    }

}
