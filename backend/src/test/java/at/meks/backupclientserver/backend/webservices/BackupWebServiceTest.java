package at.meks.backupclientserver.backend.webservices;

import at.meks.backupclientserver.backend.services.BackupService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BackupWebServiceTest {

    @Mock
    private BackupService backupService;

    @InjectMocks
    private BackupWebService webService;

    @Test
    public void whenBackupFileThenBackServiceIsInvoked() {
        String relativePath = "expRltvPth";
        String hostName = "hstNm";
        String backupedPath = "bckpdPth";
        MultipartFile expectedFile = Mockito.mock(MultipartFile.class);

        webService.backupFile(expectedFile, relativePath, hostName, backupedPath);

        verify(backupService).backup(same(expectedFile), eq(hostName), eq(backupedPath), eq(relativePath));
    }
}
