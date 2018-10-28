package at.meks.clientserverbackup.client;

import at.meks.clientserverbackup.client.backupmanager.BackupManager;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

class FileChangeHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private BackupManager backupManager;

    void fileChanged(WatchEvent.Kind kind, Path path) {
        if (kind == ENTRY_CREATE) {
            backupManager.created(path);
        } else if (kind == ENTRY_MODIFY) {
            backupManager.modified(path);
        } else if (kind == ENTRY_DELETE) {
            backupManager.deleted(path);
        } else {
            logger.error("Unknown WatchEvent.Kind: {}", kind);
        }
    }
}
