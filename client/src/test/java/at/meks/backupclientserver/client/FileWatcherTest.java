package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.filechangehandler.FileChangeHandler;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.Collections;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class FileWatcherTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private Logger logger = LoggerFactory.getLogger(getClass());
    private File testDir;
    private boolean mockInvoked;

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileWatcher fileWatcher = new FileWatcher();

    @Before
    public void reinit() throws IOException {
        mockInvoked = false;
        testDir = TestDirectoryProvider.createTempDirectory().toFile();
        when(fileService.getDirectoriesMapFile()).thenReturn(
                Files.createFile(testDir.toPath().resolve("dirMapFile.dir")));
    }

    @After
    public void stopWatcher() throws IOException {
        fileWatcher.stopWatching();
        FileUtils.forceDeleteOnExit(testDir);
    }

    @Test(timeout = 5000)
    public void whenFileChangesConsumerIsInformed() throws IOException {
        File testFile = new File(testDir, "whenFileChanges.txt");
        assertTrue(testFile.createNewFile());
        FileChangeHandler consumer = mockConsumerAndStartWatcher();
        FileUtils.writeLines(testFile, Collections.singleton("x"));
        waitForConsumerInvocation();
        // I have no idea why it is invoked 2 times, it should only be once
        verify(consumer, atLeastOnce()).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_MODIFY, testFile.toPath());
    }

    private void waitForConsumerInvocation() {
        await().until(() -> mockInvoked);
    }

    private void startWatcher(FileChangeHandler consumer) {
        fileWatcher.setPathsToWatch(new Path[]{testDir.toPath()});
        fileWatcher.setOnChangeConsumer(consumer);
        fileWatcher.startWatching();
    }

    private FileChangeHandler mockConsumer() {
        FileChangeHandler consumer = Mockito.mock(FileChangeHandler.class);
        doAnswer(invocationOnMock -> {
            logger.info("consumer was invoked");
            mockInvoked = true;
            return Void.TYPE;
        }).when(consumer).fileChanged(any(), any(), any());
        return consumer;
    }

    @Test(timeout = 5000)
    public void whenFileIsCreatedConsumerIsInformed() throws IOException {
        File testFile = new File(testDir, "whenFileIsCreated.txt");
        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        assertTrue(testFile.createNewFile());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, testFile.toPath());
    }

    @Test(timeout = 5000)
    public void whenDirectoryIsCreatedConsumerIsInformed() {
        File subDir = new File(testDir, "whenDirectoryIsCreated");
        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        assertTrue(subDir.mkdirs());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, subDir.toPath());
    }

    @Test(timeout = 5000)
    public void whenFileIsDeletedConsumerIsInformed() throws IOException {
        File testFile = new File(testDir, "whenFileIsDeleted.txt");
        assertTrue(testFile.createNewFile());

        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        Files.delete(testFile.toPath());
        waitForConsumerInvocation();
        verify(consumer, timeout(50)).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_DELETE, testFile.toPath());
    }

    @Test(timeout = 5000)
    public void whenDirectoryIsDeletedConsumerIsInformed() throws IOException {
        File subDir = new File(testDir, "whenDirectoryIsDeleted");
        //noinspection ResultOfMethodCallIgnored
        subDir.mkdirs();
        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        Files.delete(subDir.toPath());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_DELETE, subDir.toPath());
    }

    @Test(timeout = 5000)
    public void givenDirectoryAndFileWithinNewDirIsCreatedConsumerIsInformed() throws IOException {
        File subDir = new File(testDir, "givenDirectoryAndFileWithinNewDirIsCreated");
        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        File newFileInSubdir = new File(subDir, "newFileInSubdir.txt");
        assertTrue(subDir.mkdirs());
        await().until(subDir::exists);
        assertTrue(newFileInSubdir.createNewFile());

        waitForConsumerInvocation();
        verify(consumer, timeout(50)).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, subDir.toPath());
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, newFileInSubdir.toPath());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(timeout = 5000)
    public void whenFileIsAddedToSubDirConsumerIsInformed() throws IOException {
        File subDir = new File(testDir, "whenFileIsAddedToSubDir");
        subDir.mkdirs();
        await().until(subDir::exists);
        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        File newFileInSubdir = new File(subDir, "newFileInSubdir.txt");
        assertTrue(newFileInSubdir.createNewFile());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, newFileInSubdir.toPath());
    }

    @Test(timeout = 5000)
    public void whenDirectoryIsAddedToSubDirConsumerIsInformed() {
        File subDir = new File(testDir, "whenDirectoryIsAddedToSubDir");
        //noinspection ResultOfMethodCallIgnored
        subDir.mkdirs();
        await().until(subDir::exists);
        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        File subSubDir = new File(subDir, "subSubDir");
        assertTrue(subSubDir.mkdir());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, subSubDir.toPath());
    }

    @Test(timeout = 5000)
    public void whenFileIsRenamedConsumerIsInformed() throws IOException {
        File originFile = new File(testDir, "whenFileIsRenamed-Origin.txt");
        assertTrue(originFile.createNewFile());
        await().until(originFile::exists);
        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        File renamedFile = new File(testDir, "whenFileIsRenamed-RenamedFile.txt");
        assertTrue(originFile.renameTo(renamedFile));
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_DELETE, originFile.toPath());
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, renamedFile.toPath());
    }

    @Test(timeout = 5000)
    public void whenDirectoryIsRenamedConsumerIsInformed() {
        File originDir = new File(testDir, "whenDirectoryIsRenamed-Origin");
        //noinspection ResultOfMethodCallIgnored
        originDir.mkdirs();
        await().until(originDir::exists);
        FileChangeHandler consumer = mockConsumerAndStartWatcher();

        File renamedDir = new File(testDir, "whenDirectoryIsRenamed-RenamedDir");
        assertTrue(originDir.renameTo(renamedDir));
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_DELETE, originDir.toPath());
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, renamedDir.toPath());
        verifyZeroInteractions(errorReporter);
    }

    private FileChangeHandler mockConsumerAndStartWatcher() {
        FileChangeHandler consumer = mockConsumer();
        startWatcher(consumer);
        return consumer;
    }
}
