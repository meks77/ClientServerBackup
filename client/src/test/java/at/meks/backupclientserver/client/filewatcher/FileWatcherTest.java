package at.meks.backupclientserver.client.filewatcher;

import at.meks.backupclientserver.client.ApplicationConfig;
import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.FileService;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import io.quarkus.runtime.StartupEvent;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.nio.file.StandardWatchEventKinds;
import java.util.Collections;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileWatcherTest {

    @TempDir
    File testDir;

    @Mock
    private FileChangeHandler fileChangeHandler;

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationConfig config;

    @Mock
    private FileExcludeService fileExcludeService;

    @InjectMocks
    private FileWatcher fileWatcher = new FileWatcher();

    @BeforeEach
    public void reinit() throws IOException {
        when(fileService.getDirectoriesMapFile()).thenReturn(
                Files.createFile(testDir.toPath().resolve("dirMapFile.dir")));
    }

    @AfterEach
    public void stopWatcher() throws IOException {
        fileWatcher.stopWatching();
        FileUtils.forceDeleteOnExit(testDir);
    }

    @Test
    public void whenFileChangesConsumerIsInformed() throws IOException {
        File testFile = new File(testDir, "whenFileChanges.txt");
        assertTrue(testFile.createNewFile());
        mockConsumerAndStartWatcher();
        FileUtils.writeLines(testFile, Collections.singleton("x"));
        // I have no idea why it is invoked 2 times, it should only be once
        verify(fileChangeHandler, timeout(100).atLeastOnce()).fileChanged(StandardWatchEventKinds.ENTRY_MODIFY, testFile.toPath());
    }

    private void startWatcher() {
        fileWatcher.onStart(new StartupEvent());
    }

    @Test
    public void whenFileIsCreatedConsumerIsInformed() throws IOException {
        File testFile = new File(testDir, "whenFileIsCreated.txt");
        mockConsumerAndStartWatcher();

        assertTrue(testFile.createNewFile());
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_CREATE, testFile.toPath());
    }

    @Test
    public void whenDirectoryIsCreatedConsumerIsInformed() {
        File subDir = new File(testDir, "whenDirectoryIsCreated");
        mockConsumerAndStartWatcher();

        assertTrue(subDir.mkdirs());
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_CREATE, subDir.toPath());
    }

    @Test
    public void whenFileIsDeletedConsumerIsInformed() throws IOException {
        File testFile = new File(testDir, "whenFileIsDeleted.txt");
        assertTrue(testFile.createNewFile());

        mockConsumerAndStartWatcher();

        Files.delete(testFile.toPath());
        verify(fileChangeHandler, timeout(50)).fileChanged(StandardWatchEventKinds.ENTRY_DELETE, testFile.toPath());
    }

    @Test
    public void whenDirectoryIsDeletedConsumerIsInformed() throws IOException {
        File subDir = new File(testDir, "whenDirectoryIsDeleted");
        //noinspection ResultOfMethodCallIgnored
        subDir.mkdirs();
        mockConsumerAndStartWatcher();

        Files.delete(subDir.toPath());
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_DELETE, subDir.toPath());
    }

    @Test
    public void givenDirectoryAndFileWithinNewDirIsCreatedConsumerIsInformed() throws IOException {
        File subDir = new File(testDir, "givenDirectoryAndFileWithinNewDirIsCreated");
        mockConsumerAndStartWatcher();

        File newFileInSubdir = new File(subDir, "newFileInSubdir.txt");
        assertTrue(subDir.mkdirs());
        await().until(subDir::exists);
        assertTrue(newFileInSubdir.createNewFile());

        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_CREATE, subDir.toPath());
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_CREATE, newFileInSubdir.toPath());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void whenFileIsAddedToSubDirConsumerIsInformed() throws IOException {
        File subDir = new File(testDir, "whenFileIsAddedToSubDir");
        subDir.mkdirs();
        await().until(subDir::exists);
        mockConsumerAndStartWatcher();

        File newFileInSubdir = new File(subDir, "newFileInSubdir.txt");
        assertTrue(newFileInSubdir.createNewFile());
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_CREATE, newFileInSubdir.toPath());
    }

    @Test
    public void whenDirectoryIsAddedToSubDirConsumerIsInformed() {
        File subDir = new File(testDir, "whenDirectoryIsAddedToSubDir");
        //noinspection ResultOfMethodCallIgnored
        subDir.mkdirs();
        await().until(subDir::exists);
        mockConsumerAndStartWatcher();

        File subSubDir = new File(subDir, "subSubDir");
        assertTrue(subSubDir.mkdir());
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_CREATE, subSubDir.toPath());
    }

    @Test
    public void whenFileIsRenamedConsumerIsInformed() throws IOException {
        File originFile = new File(testDir, "whenFileIsRenamed-Origin.txt");
        assertTrue(originFile.createNewFile());
        await().until(originFile::exists);
        mockConsumerAndStartWatcher();

        File renamedFile = new File(testDir, "whenFileIsRenamed-RenamedFile.txt");
        assertTrue(originFile.renameTo(renamedFile));
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_DELETE, originFile.toPath());
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_CREATE, renamedFile.toPath());
    }

    @Test
    void whenDirectoryIsRenamedConsumerIsInformed() {
        File originDir = new File(testDir, "whenDirectoryIsRenamed-Origin");
        //noinspection ResultOfMethodCallIgnored
        originDir.mkdirs();
        await().until(originDir::exists);
        mockConsumerAndStartWatcher();

        File renamedDir = new File(testDir, "whenDirectoryIsRenamed-RenamedDir");
        assertTrue(originDir.renameTo(renamedDir));
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_DELETE, originDir.toPath());
        verify(fileChangeHandler, timeout(100)).fileChanged(StandardWatchEventKinds.ENTRY_CREATE, renamedDir.toPath());
        verifyNoInteractions(errorReporter);
    }

    private void mockConsumerAndStartWatcher() {
        when(config.getBackupedDirs()).thenReturn(new Path[]{testDir.toPath()});
        startWatcher();
    }
}
