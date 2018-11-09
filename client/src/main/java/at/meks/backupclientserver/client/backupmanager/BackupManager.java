package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ClientBackupException;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private BlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();
    private Thread queueReaderThread;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @Inject
    private BackupRemoteService backupRemoteService;

    @Inject
    private void start() {
        if (queueReaderThread == null || !queueReaderThread.isAlive()) {
            queueReaderThread = new Thread(this::backupQueueItems);
            queueReaderThread.setDaemon(true);
            queueReaderThread.start();
        }
    }

    private void backupQueueItems() {
        try {
            //noinspection InfiniteLoopStatement
            do {
                TodoEntry todoEntry = backupQueue.take();
                FileTime lastModifiedTime = Files.getLastModifiedTime(todoEntry.getChangedFile());
                int delay = 1000;
                if (lastModifiedTime.toMillis() > System.currentTimeMillis() - 1000) {
                    scheduledExecutorService.schedule(() -> addForBackup(todoEntry), delay, TimeUnit.MILLISECONDS);
                } else {
                    backup(todoEntry);
                }
            } while (true);
        } catch (InterruptedException e) {
            logger.error("listening for Backup items was interrupted", e);
        } catch (IOException e) {
            throw new ClientBackupException("Error while trying to backup file", e);
        }
    }

    private void backup(TodoEntry item) {
        logger.info("backup {}", item);
        if (item.getChangedFile().toFile().isFile() &&
                item.getType() != PathChangeType.DELETED &&
                !isFileUpToDate(item)) {
            backupFile(item);
        }
    }

    private boolean isFileUpToDate(TodoEntry item) {
        boolean fileUpToDate = backupRemoteService.isFileUpToDate(item.getWatchedPath(), item.getChangedFile());
        logger.info("the file {} is update2date: {}", item.getChangedFile(), fileUpToDate);
        return fileUpToDate;
    }

    private void backupFile(TodoEntry item) {
        logger.info("backup file {}", item.getChangedFile());
        backupRemoteService.backupFile(item.getWatchedPath(), item.getChangedFile());
    }

    public void addForBackup(TodoEntry item) {
        backupQueue.add(item);
    }
}
