package at.meks.clientserverbackup.client;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

class BackupManager {

    private enum PathChangeType {
        CREATED, MODIFIED, DELETED
    }

    private class TodoEntry{

        private final PathChangeType type;
        private final Path path;

        private TodoEntry(PathChangeType type, Path path) {
            this.type = type;
            this.path = path;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("type", type)
                    .add("path", path)
                    .toString();
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    private BlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();

    public BackupManager() {
        new Thread(this::backupQueueItems).start();
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

    void created(Path path) {
        backupQueue.add(new TodoEntry(PathChangeType.CREATED, path));
    }

    void modified(Path path) {
        backupQueue.add(new TodoEntry(PathChangeType.MODIFIED, path));
    }

    void deleted(Path path) {
        backupQueue.add(new TodoEntry(PathChangeType.DELETED, path));
    }
}
