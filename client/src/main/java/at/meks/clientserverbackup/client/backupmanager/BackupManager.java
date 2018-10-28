package at.meks.clientserverbackup.client.backupmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class BackupManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private BlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();
    private final Thread queueReaderThread;

    public BackupManager() {
        queueReaderThread = new Thread(this::backupQueueItems);
        queueReaderThread.setDaemon(true);
        queueReaderThread.start();
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
        //TODO ask server if backup is necessary
        //TODO send file for backup to server
    }

    public void created(Path path) {
        backupQueue.add(new TodoEntry(PathChangeType.CREATED, path));
    }

    public void modified(Path path) {
        backupQueue.add(new TodoEntry(PathChangeType.MODIFIED, path));
    }

    public void deleted(Path path) {
        backupQueue.add(new TodoEntry(PathChangeType.DELETED, path));
    }
}
