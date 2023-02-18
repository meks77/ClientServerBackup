package at.meks.backup.client.model;

import java.nio.file.Path;

public interface Events {

    record FileChangedEvent(Path file) {  }

    record BackupCommand(Path file) {

    }

    void fireScanDirectories();

    void fireFileChanged(FileChangedEvent event);

    void fireFileNeedsBackup(BackupCommand event);

    void register(FileEventListener fileEventListener);

    void register(ScanDirectoryCommandListener scanDirectoryCommandListener);
}
