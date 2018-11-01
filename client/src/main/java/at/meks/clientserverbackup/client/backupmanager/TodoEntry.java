package at.meks.clientserverbackup.client.backupmanager;

import com.google.common.base.MoreObjects;

import java.nio.file.Path;

public class TodoEntry {

    private final PathChangeType type;
    private final Path changedFile;
    private final Path watchedPath;

    public TodoEntry(PathChangeType type, Path changedFile, Path watchedPath) {
        this.type = type;
        this.changedFile = changedFile;
        this.watchedPath = watchedPath;
    }

    PathChangeType getType() {
        return type;
    }

    Path getChangedFile() {
        return changedFile;
    }

    Path getWatchedPath() {
        return watchedPath;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("watchedPath", watchedPath)
                .add("changedFile", changedFile)
                .toString();
    }
}
