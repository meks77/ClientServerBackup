package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.backup.BackupService;
import at.meks.backupclientserver.backend.services.file.UploadService;
import at.meks.backupclientserver.common.service.BackupCommandArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BackupWebServiceTest extends AbstractWebServiceTest {

    @Mock
    private BackupService backupService;

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private BackupWebService webService;

    @Test
    public void whenBackupFileThenBackupServiceIsInvoked() {
        mockExceptionHandlerForRunnable();
        String[] relativePath = new String[] {"expRltvPth"};
        String hostName = "hstNm";
        String backupedPath = "bckpdPth";
        String uploadedFileRelativePath = "pathToUploadedFle";
        String fileName = "backedUpFileName.txt";
        Path expectedAbsolutPath = mock(Path.class);
        when(uploadService.getAbsolutePath(uploadedFileRelativePath)).thenReturn(expectedAbsolutPath);

        webService.backupFile(
                new BackupCommandArgs(uploadedFileRelativePath, hostName, relativePath, backupedPath, fileName));

        verify(backupService).backup(same(expectedAbsolutPath),
                eq(FileInputArgs.aFileInputArgs().hostName(hostName).backupedPath(backupedPath)
                        .relativePath(relativePath).fileName(fileName).build()));
    }

    @Test
    public void whenIsFileUp2dateThenBackupServiceIsInvoked() {
        mockExceptionHandlerForCallable();
        String expectedBackupPath = "theExpectedBackupPath";
        String expectedFileName = "theExpectedFileName";
        String expectedHostName = "theExpectedHostName";
        String[] expectedRelativePath = new String[] {"theExpectedRelativePath"};
        String expectedMd5Checksum = "theExpectedMd5Checksum";


        FileUp2dateInput fileUp2dateInput = new FileUp2dateInput();
        fileUp2dateInput.setBackupedPath(expectedBackupPath);
        fileUp2dateInput.setFileName(expectedFileName);
        fileUp2dateInput.setHostName(expectedHostName);
        fileUp2dateInput.setRelativePath(expectedRelativePath);
        fileUp2dateInput.setMd5Checksum(expectedMd5Checksum);

        webService.isFileUp2date(fileUp2dateInput);

        verify(backupService).isFileUpToDate(
                same(fileUp2dateInput),
                eq(expectedMd5Checksum));
    }

    @Test
    public void givenFileIsUp2dateWhenIsFileUp2dateReturnTrue() {
        mockExceptionHandlerForCallable();
        when(backupService.isFileUpToDate(any(), any())).thenReturn(true);

        FileUp2dateResult result = webService.isFileUp2date(new FileUp2dateInput());
        assertThat(result).isNotNull();
        assertThat(result.isUp2date()).isTrue();
    }

    @Test
    public void givenFileIsNotUp2dateWhenIsFileUp2dateReturnFalse() {
        mockExceptionHandlerForCallable();
        when(backupService.isFileUpToDate(any(), any())).thenReturn(false);

        FileUp2dateResult result = webService.isFileUp2date(new FileUp2dateInput());
        assertThat(result).isNotNull();
        assertThat(result.isUp2date()).isFalse();
    }

    @Test
    public void whenDeleteFileThenCallIsDelegatedToBackupService() {
        mockExceptionHandlerForRunnable();
        FileInputArgs fileInputArgs = new FileInputArgs();
        webService.deletePath(fileInputArgs);
        verify(backupService).delete(same(fileInputArgs));
    }

    @Test
    public void whenBackupFileThenExceptionHandlerIsInvoked() {
        mockExceptionHandlerForCallable();
        mockExceptionHandlerForRunnable();
        verifyExceptionHandlerIsInvokedAndNothingElse(() -> webService.backupFile(null),
                backupService);
    }

    @Test
    public void whenIsFileUp2dateThenExceptionHandlerIsInvoked() {
        mockExceptionHandlerForCallable();
        mockExceptionHandlerForRunnable();
        verifyExceptionHandlerIsInvokedAndNothingElse(() -> webService.isFileUp2date(null), backupService);
    }

    @Test
    public void whenDeletePathThenExceptionHandlerIsInvoked() {
        verifyExceptionHandlerIsInvokedAndNothingElse(() -> webService.deletePath(null), backupService);
    }

}
