package at.meks.backupclientserver.client.filechangehandler;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface FileChangeHandler {

    void fileChanged(WatchEvent.Kind<?> kind, Path changedFile);

}
