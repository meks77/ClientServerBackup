package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.BackupService;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BackupWebServiceTest {

    @Mock
    private BackupService backupService;

    @InjectMocks
    private BackupWebService webService;

    @Test
    public void whenBackupFileThenBackupServiceIsInvoked() {
        String relativePath = "expRltvPth";
        String hostName = "hstNm";
        String backupedPath = "bckpdPth";
        MultipartFile expectedFile = Mockito.mock(MultipartFile.class);

        webService.backupFile(expectedFile, relativePath, hostName, backupedPath);

        verify(backupService).backup(same(expectedFile), eq(hostName), eq(backupedPath), eq(relativePath));
    }

    @Test
    public void whenIsFileUp2dateThenBackupServiceIsInvoked() {
        String expectedBackupPath = "theExpectedBackupPath";
        String expectedFileName = "theExpectedFileName";
        String expectedHostName = "theExpectedHostName";
        String expectedRelativePath = "theExpectedRelativePath";
        String expectedMd5Checksum = "theExpectedMd5Checksum";

        FileUp2dateInput fileUp2dateInput = new FileUp2dateInput();
        fileUp2dateInput.setBackupedPath(expectedBackupPath);
        fileUp2dateInput.setFileName(expectedFileName);
        fileUp2dateInput.setHostName(expectedHostName);
        fileUp2dateInput.setRelativePath(expectedRelativePath);
        fileUp2dateInput.setMd5Checksum(expectedMd5Checksum);

        webService.isFileUp2date(fileUp2dateInput);

        verify(backupService).isFileUpToDate(expectedHostName, expectedBackupPath, expectedRelativePath,
                expectedFileName, expectedMd5Checksum);
    }

    @Test
    public void givenFileIsUp2dateWhenIsFileUp2dateReturnTrue() {
        when(backupService.isFileUpToDate(any(), any(), any(), any(), any())).thenReturn(true);

        FileUp2dateResult result = webService.isFileUp2date(new FileUp2dateInput());
        assertThat(result).isNotNull();
        assertThat(result.isUp2date()).isTrue();
    }

    @Test
    public void givenFileIsNotUp2dateWhenIsFileUp2dateReturnFalse() {
        when(backupService.isFileUpToDate(any(), any(), any(), any(), any())).thenReturn(false);

        FileUp2dateResult result = webService.isFileUp2date(new FileUp2dateInput());
        assertThat(result).isNotNull();
        assertThat(result.isUp2date()).isFalse();
    }
}
