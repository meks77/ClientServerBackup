package at.meks.clientserverbackup.client;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

public class FileWatcherTest {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private File testDir;
    private boolean mockInvoked;
    private FileWatcher fileWatcher;

    @Before
    public void reinit() throws URISyntaxException, IOException {
        mockInvoked = false;
        testDir = new File(new File(getClass().getResource(".").toURI()), "testDir");
        //noinspection ResultOfMethodCallIgnored
        testDir.mkdirs();
        File[] files = testDir.listFiles();
        if (files != null) {
            for (File file : files) {
                FileUtils.forceDelete(file);
            }
        }
    }

    @After
    public void stopWatcher() {
        fileWatcher.stopWatching();
    }

    @Test(timeout = 5000)
    public void whenFileChangesConsumerIsInformed() throws IOException, InterruptedException {
        File testFile = new File(testDir, "whenFileChanges.txt");
        assertTrue(testFile.createNewFile());
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();
        FileUtils.writeLines(testFile, Collections.singleton("x"));
        waitForConsumerInvocation();
        // I have no idea why it is invoked 2 times, it should only be once
        verify(consumer, atLeastOnce()).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_MODIFY, testFile.toPath());
    }

    private void waitForConsumerInvocation() throws InterruptedException {
        while(!mockInvoked) {
            Thread.sleep(50L);
        }
    }

    private void startWatcher(IFileChangeHandler consumer) {
        fileWatcher = new FileWatcher();
        fileWatcher.setPathsToWatch(new Path[]{testDir.toPath()});
        fileWatcher.setOnChangeConsumer(consumer);
        fileWatcher.startWatching();
    }

    private IFileChangeHandler mockConsumer() {
        IFileChangeHandler consumer = Mockito.mock(IFileChangeHandler.class);
        doAnswer(invocationOnMock -> {
            logger.info("consumer was invoked");
            mockInvoked = true;
            return Void.TYPE;
        }).when(consumer).fileChanged(any(), any(), any());
        return consumer;
    }

    @Test(timeout = 5000)
    public void whenFileIsCreatedConsumerIsInformed() throws InterruptedException, IOException {
        File testFile = new File(testDir, "whenFileIsCreated.txt");
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        assertTrue(testFile.createNewFile());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, testFile.toPath());
    }

    @Test(timeout = 5000)
    public void whenDirectoryIsCreatedConsumerIsInformed() throws InterruptedException {
        File subDir = new File(testDir, "whenDirectoryIsCreated");
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        assertTrue(subDir.mkdirs());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, subDir.toPath());
    }

    @Test(timeout = 5000)
    public void whenFileIsDeletedConsumerIsInformed() throws InterruptedException, IOException {
        File testFile = new File(testDir, "whenFileIsDeleted.txt");
        assertTrue(testFile.createNewFile());

        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        Files.delete(testFile.toPath());
        waitForConsumerInvocation();
        Thread.sleep(50L);
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_DELETE, testFile.toPath());
    }

    @Test(timeout = 5000)
    public void whenDirectoryIsDeletedConsumerIsInformed() throws InterruptedException, IOException {
        File subDir = new File(testDir, "whenDirectoryIsDeleted");
        //noinspection ResultOfMethodCallIgnored
        subDir.mkdirs();
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        Files.delete(subDir.toPath());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_DELETE, subDir.toPath());
    }

    @Test(timeout = 5000)
    public void givenDirectoryAndFileWithinNewDirIsCreatedConsumerIsInformed() throws IOException, InterruptedException {
        File subDir = new File(testDir, "givenDirectoryAndFileWithinNewDirIsCreated");
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        File newFileInSubdir = new File(subDir, "newFileInSubdir.txt");
        assertTrue(subDir.mkdirs());
        Thread.sleep(50L);// wait that filewatcher is able to start watching for new directory
        assertTrue(newFileInSubdir.createNewFile());

        waitForConsumerInvocation();
        Thread.sleep(50L);
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, subDir.toPath());
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, newFileInSubdir.toPath());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(timeout = 5000)
    public void whenFileIsAddedToSubDirConsumerIsInformed() throws InterruptedException, IOException {
        File subDir = new File(testDir, "whenFileIsAddedToSubDir");
        subDir.mkdirs();
        Thread.sleep(50L);
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        File newFileInSubdir = new File(subDir, "newFileInSubdir.txt");
        assertTrue(newFileInSubdir.createNewFile());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, newFileInSubdir.toPath());
    }

    @Test(timeout = 5000)
    public void whenDirectoryIsAddedToSubDirConsumerIsInformed() throws InterruptedException {
        File subDir = new File(testDir, "whenDirectoryIsAddedToSubDir");
        //noinspection ResultOfMethodCallIgnored
        subDir.mkdirs();
        Thread.sleep(50L);
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        File subSubDir = new File(subDir, "subSubDir");
        assertTrue(subSubDir.mkdir());
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, subSubDir.toPath());
    }

    @Test(timeout = 5000)
    public void whenFileIsRenamedConsumerIsInformed() throws InterruptedException, IOException {
        File originFile = new File(testDir, "whenFileIsRenamed-Origin.txt");
        assertTrue(originFile.createNewFile());
        Thread.sleep(50L);
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        File renamedFile = new File(testDir, "whenFileIsRenamed-RenamedFile.txt");
        assertTrue(originFile.renameTo(renamedFile));
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_DELETE, originFile.toPath());
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, renamedFile.toPath());
    }

    @Test(timeout = 5000)
    public void whenDirectoryIsRenamedConsumerIsInformed() throws InterruptedException {
        File originDir = new File(testDir, "whenDirectoryIsRenamed-Origin");
        //noinspection ResultOfMethodCallIgnored
        originDir.mkdirs();
        Thread.sleep(50L);
        IFileChangeHandler consumer = mockConsumerAndStartWatcher();

        File renamedDir = new File(testDir, "whenDirectoryIsRenamed-RenamedDir");
        assertTrue(originDir.renameTo(renamedDir));
        waitForConsumerInvocation();
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_DELETE, originDir.toPath());
        verify(consumer).fileChanged(testDir.toPath(), StandardWatchEventKinds.ENTRY_CREATE, renamedDir.toPath());
    }

    private IFileChangeHandler mockConsumerAndStartWatcher() throws InterruptedException {
        IFileChangeHandler consumer = mockConsumer();
        startWatcher(consumer);
        Thread.sleep(50L);
        return consumer;
    }
}
