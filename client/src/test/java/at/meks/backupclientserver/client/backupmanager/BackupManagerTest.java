package at.meks.backupclientserver.client.backupmanager;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingDeque;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BackupManagerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Spy
    private LinkedBlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();

    @Mock
    private BackupRemoteService backupRemoteService;

    @InjectMocks
    private BackupManager manager = new BackupManager();

    @Before
    public void mockConfig() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method startMethod = BackupManager.class.getDeclaredMethod("start");
        startMethod.setAccessible(true);
        startMethod.invoke(manager);
    }

    @Test
    public void givenModifiedFileWhenAddForBackupThenHttpServerRequestIsExecutedAsExpected() throws URISyntaxException, InterruptedException {
        Path uplodedFilePath = Paths.get(getClass().getResource("/mappings/backup-successfull.json").toURI());
        Path backupSetPath = Paths.get(getClass().getResource("/").toURI());
        manager.addForBackup(new TodoEntry(PathChangeType.MODIFIED,
                uplodedFilePath,
                backupSetPath));
        waitForQueueItemIsProcessed();
        verify(backupRemoteService).backupFile(backupSetPath, uplodedFilePath);
    }

    private void waitForQueueItemIsProcessed() throws InterruptedException {
        verify(backupQueue, timeout(2000).times(2)).take();
    }

    @Test
    public void givenFileMd5EqualsWhenAddForBackupThenFileIsntBackuped() throws InterruptedException,
            URISyntaxException {
        Path uplodedFilePath = Paths.get(getClass().getResource("/mappings/backup-successfull.json").toURI());
        Path backupSetPath = Paths.get(getClass().getResource("/").toURI());
        when(backupRemoteService.isFileUpToDate(any(), any())).thenReturn(true);

        manager.addForBackup(new TodoEntry(PathChangeType.MODIFIED,
                uplodedFilePath,
                backupSetPath));
        waitForQueueItemIsProcessed();

        verify(backupRemoteService).isFileUpToDate(backupSetPath, uplodedFilePath);
        verify(backupRemoteService, never()).backupFile(any(), any());
    }

    @Test
    public void givenFileMd5DiffersWhennAddForBackupThenFileIsBackuped() throws InterruptedException,
            URISyntaxException {
        Path uplodedFilePath = Paths.get(getClass().getResource("/mappings/backup-successfull.json").toURI());
        Path backupSetPath = Paths.get(getClass().getResource("/").toURI());
        when(backupRemoteService.isFileUpToDate(any(), any())).thenReturn(false);

        manager.addForBackup(new TodoEntry(PathChangeType.MODIFIED,
                uplodedFilePath,
                backupSetPath));
        waitForQueueItemIsProcessed();

        verify(backupRemoteService).isFileUpToDate(backupSetPath, uplodedFilePath);
        verify(backupRemoteService).backupFile(backupSetPath, uplodedFilePath);
    }

    @Test
    public void givenWritingAFileWhileAddForBackupThenBackupOfFileIsDoneAfterWritingFinished() throws IOException,
            InterruptedException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path hugeFile = Files.createTempFile(backupSetPath, "hugeFile", ".txt");
        Thread fileWritingThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            long writeTimeDurationInMs = 5000;
            while (System.currentTimeMillis() < startTime + writeTimeDurationInMs) {
                try {
                    FileUtils.write(hugeFile.toFile(), "x", "utf8");
                    Thread.sleep(100);
                } catch (Exception e) {
                    Assert.fail("couldn't write file. Excepion occured: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        fileWritingThread.start();
        manager.addForBackup(new TodoEntry(PathChangeType.MODIFIED, hugeFile, backupSetPath));

        verify(backupRemoteService, timeout(1000).times(0)).isFileUpToDate(backupSetPath, hugeFile);
        verify(backupRemoteService, timeout(1000).times(0)).backupFile(backupSetPath, hugeFile);
        fileWritingThread.join();
        verify(backupRemoteService, timeout(5000).times(1)).isFileUpToDate(backupSetPath, hugeFile);
        verify(backupRemoteService, timeout(1000).times(1)).backupFile(backupSetPath, hugeFile);
    }

}
