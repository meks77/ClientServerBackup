package at.meks.clientserverbackup.client;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

interface FileChangeHandler {

    void fileChanged(Path watchedRootPath, WatchEvent.Kind kind, Path changedFile);

}
