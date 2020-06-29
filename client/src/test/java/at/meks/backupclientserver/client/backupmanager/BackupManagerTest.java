package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.excludes.FileExcludeService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
public class BackupManagerTest {

    private static final String FILE_NAME_IN_TEST_SOURCE_FOR_BACKUP = "fileToBackup.txt";

    @Spy
    private LinkedBlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();

    @Mock
    private BackupService backupService;

    @Mock
    private FileExcludeService fileExcludeService;

    @InjectMocks
    private BackupManager manager = new BackupManager();

    @BeforeEach
    public void mockConfig() {
        manager.start();
    }

    @Test
    public void givenModifiedFileWhenAddForBackupThenHttpServerRequestIsExecutedAsExpected() throws URISyntaxException, InterruptedException {
        Path uplodedFilePath = Paths.get(getClass().getResource(FILE_NAME_IN_TEST_SOURCE_FOR_BACKUP).toURI());
        Path backupSetPath = Paths.get(getClass().getResource("/").toURI());
        manager.addForBackup(new TodoEntry(PathChangeType.MODIFIED,
                uplodedFilePath,
                backupSetPath));
        waitForQueueItemIsProcessed();
        verify(backupService).backupFile(uplodedFilePath);
    }

    private void waitForQueueItemIsProcessed() throws InterruptedException {
        verify(backupQueue, timeout(2000).times(2)).take();
    }

    @Test
    public void givenFileMd5EqualsWhenAddForBackupThenFileIsntBackuped() throws InterruptedException,
            URISyntaxException {
        Path uplodedFilePath = Paths.get(getClass().getResource(FILE_NAME_IN_TEST_SOURCE_FOR_BACKUP).toURI());
        Path backupSetPath = Paths.get(getClass().getResource("/").toURI());
        when(backupService.isFileUpToDate(any())).thenReturn(true);

        manager.addForBackup(new TodoEntry(PathChangeType.MODIFIED,
                uplodedFilePath,
                backupSetPath));
        waitForQueueItemIsProcessed();

        verify(backupService).isFileUpToDate(uplodedFilePath);
        verify(backupService, never()).backupFile(any());
    }

    @Test
    public void givenFileMd5DiffersWhennAddForBackupThenFileIsBackuped() throws InterruptedException,
            URISyntaxException {
        Path uplodedFilePath = Paths.get(getClass().getResource(FILE_NAME_IN_TEST_SOURCE_FOR_BACKUP).toURI());
        Path backupSetPath = Paths.get(getClass().getResource("/").toURI());
        when(backupService.isFileUpToDate(any())).thenReturn(false);

        manager.addForBackup(new TodoEntry(PathChangeType.MODIFIED,
                uplodedFilePath,
                backupSetPath));
        waitForQueueItemIsProcessed();

        verify(backupService).isFileUpToDate(uplodedFilePath);
        verify(backupService).backupFile(uplodedFilePath);
    }

    @SneakyThrows
    @Test
    public void givenDeletedEntryWhenBackupThenBackupRemoteServiceDeleteIsInvoked(@TempDir Path tempDir) {
        Path file = Files.createFile(tempDir.resolve("deletedFile.txt"));
        Path backupSet = tempDir.resolve("backupSet");

        manager.addForBackup(new TodoEntry(PathChangeType.DELETED, file, backupSet));

        verify(backupService, timeout(1000)).delete(file);
    }

    @Test
    public void givenExcludedFileWhenAddForBackupThenItemIsNotScheduledForBackup(@TempDir Path temporaryFolder) throws IOException {
        Path backupSet = Files.createDirectories(temporaryFolder.resolve("backupSet"));
        Path fileForBackup = Files.createFile(temporaryFolder.resolve("whatever.lock"));

        when(fileExcludeService.isFileExcludedFromBackup(fileForBackup)).thenReturn(true);

        manager.addForBackup(new TodoEntry(PathChangeType.CREATED, fileForBackup, backupSet));

        verify(backupService, Mockito.after(1000).never()).isFileUpToDate(any());
    }

}
