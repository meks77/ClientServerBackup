package at.meks.clientserverbackup.client.backupmanager;

import com.google.common.base.MoreObjects;

import java.nio.file.Path;

class TodoEntry {

    private final PathChangeType type;
    private final Path path;

    TodoEntry(PathChangeType type, Path path) {
        this.type = type;
        this.path = path;
    }

    PathChangeType getType() {
        return type;
    }

    Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("path", path)
                .toString();
    }
}
