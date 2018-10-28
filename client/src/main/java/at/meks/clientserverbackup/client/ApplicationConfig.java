package at.meks.clientserverbackup.client;

import java.nio.file.Path;
import java.nio.file.Paths;

class ApplicationConfig {

    Path[] getBackupedDirs() {
        //TODO read paths from config
        return new Path[]{Paths.get("C:\\development\\clientServerBackupDirs\\folder1"),
                Paths.get("C:\\development\\clientServerBackupDirs\\folder1")};
    }

}
