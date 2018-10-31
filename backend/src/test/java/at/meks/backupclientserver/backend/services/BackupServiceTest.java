package at.meks.backupclientserver.backend.services;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BackupServiceTest {

    @Mock
    private DirectoryService directoryService;

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

        when(multipartFile.getOriginalFilename()).thenReturn(fileNameOfBackedupFile);
        when(directoryService.getBackupSetPath(hostName, clientBackupSetPath)).thenReturn(backupSetTargetPath);

        service.backup(multipartFile, hostName, clientBackupSetPath, "expected\\target");

        verify(multipartFile).transferTo(expectedTarget);
    }

    @Test(expected = ServerBackupException.class)
    public void givenIoExceptionWhenBackupThenServerBackupExceptionIsThrown() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("expctdName.txt");
        doThrow(new IOException("ut expktd exc")).when(multipartFile).transferTo(any());
        when(directoryService.getBackupSetPath(any(), any())).thenReturn(Files.createTempDirectory("utThrw"));
        service.backup(multipartFile, "myHostName", "C:\\f1\\f2", "a\\b\\c");
    }

}
