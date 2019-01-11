package at.meks.backupclientserver.client.backupmanager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingDeque;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BackupManagerTest {

    private static final String FILE_NAME_IN_TEST_SOURCE_FOR_BACKUP = "fileToBackup.txt";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Spy
    private LinkedBlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();

    @Mock
    private BackupRemoteService backupRemoteService;

    @Mock
    private FileExcludeService fileExcludeService;

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
        Path uplodedFilePath = Paths.get(getClass().getResource(FILE_NAME_IN_TEST_SOURCE_FOR_BACKUP).toURI());
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
        Path uplodedFilePath = Paths.get(getClass().getResource(FILE_NAME_IN_TEST_SOURCE_FOR_BACKUP).toURI());
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
        Path uplodedFilePath = Paths.get(getClass().getResource(FILE_NAME_IN_TEST_SOURCE_FOR_BACKUP).toURI());
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
    public void givenDeletedEntryWhenBackupThenBackupRemoteServiceDeleteIsInvoked() {
        Path file = Paths.get("deletedFile.txt");
        Path backupSet = Paths.get("backupSet");

        manager.addForBackup(new TodoEntry(PathChangeType.DELETED, file, backupSet));

        verify(backupRemoteService, timeout(1000)).delete(backupSet, file);
    }

    @Test
    public void givenExcludedFileWhenAddForBackupThenItemIsNotScheduledForBackup() throws IOException {
        Path backupSet = temporaryFolder.newFolder("backupSet").toPath();
        Path fileForBackup = temporaryFolder.newFile("whatever.lock").toPath();

        when(fileExcludeService.isFileExcludedFromBackup(fileForBackup)).thenReturn(true);

        manager.addForBackup(new TodoEntry(PathChangeType.CREATED, fileForBackup, backupSet));

        verify(backupRemoteService, Mockito.after(1000).never()).isFileUpToDate(any(), any());
    }

}
