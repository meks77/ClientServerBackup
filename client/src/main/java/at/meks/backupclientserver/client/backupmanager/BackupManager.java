package at.meks.backupclientserver.client.backupmanager;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class BackupManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private BlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();
    private Thread queueReaderThread;

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
                backup(backupQueue.take());
            } while (true);
        } catch (InterruptedException e) {
            logger.error("listening for Backup items was interrupted", e);
        }
    }

    private void backup(TodoEntry item) {
        logger.info("backup {}", item);
        if (item.getChangedFile().toFile().isFile()) {
            if (item.getType() == PathChangeType.DELETED) {
                deleteFileOnServer(item);
            } else {
                if (!isFileUpToDate(item)) {
                    backupFile(item);
                }
            }
        }
    }

    private boolean isFileUpToDate(TodoEntry item) {
        boolean fileUpToDate = backupRemoteService.isFileUpToDate(item.getWatchedPath(), item.getChangedFile());
        logger.info("the file {} is update2date: {}", item.getChangedFile(), fileUpToDate);
        return fileUpToDate;
    }

    private void deleteFileOnServer(TodoEntry item) {
        // a ticket exists for that
    }

    private void backupFile(TodoEntry item) {
        logger.info("backup file {}", item.getChangedFile());
        backupRemoteService.backupFile(item.getWatchedPath(), item.getChangedFile());
    }

    public void addForBackup(TodoEntry item) {
        backupQueue.add(item);
    }
}
