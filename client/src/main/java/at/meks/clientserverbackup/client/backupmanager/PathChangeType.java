package at.meks.clientserverbackup.client.backupmanager;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public enum PathChangeType {
    CREATED, MODIFIED, DELETED;

    public static PathChangeType from(WatchEvent.Kind kind) {
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            return CREATED;
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            return DELETED;
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            return MODIFIED;
        }
        return null;
    }
}
