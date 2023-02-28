package at.meks.backup.client.model;

import java.nio.file.Path;

public interface Events {

    record FileChangedEvent(Path file) {  }

    void fireScanDirectories();

    void fireFileChanged(FileChangedEvent event);

    void register(FileEventListener fileEventListener);

    void register(ScanDirectoryCommandListener scanDirectoryCommandListener);
}
