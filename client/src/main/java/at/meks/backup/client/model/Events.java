package at.meks.backup.client.model;

import java.nio.file.Path;

public interface Events {

    record FileChangedEvent(Path file) {  }

    record FileNeedsBackupEvent(Path file) {
    }

    void fireFileChanged(FileChangedEvent event);

    void fireFileNeedsBackup(FileNeedsBackupEvent event);

    void register(FileEventListener fileEventListener);
}
