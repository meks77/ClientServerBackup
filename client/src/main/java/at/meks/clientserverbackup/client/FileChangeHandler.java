package at.meks.clientserverbackup.client;

import at.meks.clientserverbackup.client.backupmanager.BackupManager;
import at.meks.clientserverbackup.client.backupmanager.PathChangeType;
import at.meks.clientserverbackup.client.backupmanager.TodoEntry;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

class FileChangeHandler implements IFileChangeHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private BackupManager backupManager;

    @Override
    public void fileChanged(Path watchedRootPath, WatchEvent.Kind kind, Path changedFile) {
        PathChangeType pathChangeType = PathChangeType.from(kind);
        if (pathChangeType == null) {
            logger.error("unknown WatchEvent.Kind {}", kind);
            return;
        }
        backupManager.addForBackup(new TodoEntry(pathChangeType, changedFile, watchedRootPath));
    }

}
