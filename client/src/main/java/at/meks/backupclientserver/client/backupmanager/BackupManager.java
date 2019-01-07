package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ErrorReporter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Singleton
public class BackupManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private BlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>(100);

    private Thread queueReaderThread;

    @Inject
    private BackupRemoteService backupRemoteService;

    @Inject
    private ErrorReporter errorReporter;

    @Inject
    private void start() {
        if (queueReaderThread == null || !queueReaderThread.isAlive()) {
            queueReaderThread = new Thread(this::backupQueueItems);
            queueReaderThread.setName("backupQueueReader");
            queueReaderThread.setDaemon(true);
            queueReaderThread.start();
        }
    }

    private void backupQueueItems() {
        try {
            do {
                backup(backupQueue.take());
            } while (true);
        } catch (InterruptedException e) {
            errorReporter.reportError("listening for Backup items was interrupted", e);
            Thread.currentThread().interrupt();

        }
    }

    private void backup(TodoEntry item) {
        logger.debug("backup {}", item);
        try {
            if (item.getChangedFile().toFile().isFile() &&
                    item.getType() != PathChangeType.DELETED &&
                    !isFileUpToDate(item)) {
                backupFile(item);
            } else if (item.getType() == PathChangeType.DELETED) {
                deleteBackupedFile(item);
            }
        } catch (Exception e) {
            errorReporter.reportError("error while backing up file " + item.getChangedFile(), e);
        }
    }

    private boolean isFileUpToDate(TodoEntry item) {
        boolean fileUpToDate = backupRemoteService.isFileUpToDate(item.getWatchedPath(), item.getChangedFile());
        logger.debug("the file {} is update2date: {}", item.getChangedFile(), fileUpToDate);
        return fileUpToDate;
    }

    private void backupFile(TodoEntry item) {
        logger.info("backup file {}", item.getChangedFile());
        backupRemoteService.backupFile(item.getWatchedPath(), item.getChangedFile());
    }

    private void deleteBackupedFile(TodoEntry item) {
        logger.info("delete file {}", item.getChangedFile());
        backupRemoteService.delete(item.getWatchedPath(), item.getChangedFile());
    }

    public void addForBackup(TodoEntry item) {
        try {
            backupQueue.put(item);
        } catch (InterruptedException e) {
            errorReporter.reportError("adding backup item " + item + " to queue was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
