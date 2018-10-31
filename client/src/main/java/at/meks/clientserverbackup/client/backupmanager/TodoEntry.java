package at.meks.clientserverbackup.client.backupmanager;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoEntry todoEntry = (TodoEntry) o;
        return type == todoEntry.type &&
                Objects.equal(changedFile, todoEntry.changedFile) &&
                Objects.equal(watchedPath, todoEntry.watchedPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, changedFile, watchedPath);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("watchedPath", watchedPath)
                .add("type", type)
                .add("changedFile", changedFile)
                .toString();
    }
}
