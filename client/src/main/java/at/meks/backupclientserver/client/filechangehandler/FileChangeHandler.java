package at.meks.backupclientserver.client.filechangehandler;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface FileChangeHandler {

    void fileChanged(Path watchedRootPath, WatchEvent.Kind kind, Path changedFile);

}
