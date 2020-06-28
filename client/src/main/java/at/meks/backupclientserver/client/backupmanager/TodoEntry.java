package at.meks.backupclientserver.client.backupmanager;

import lombok.Value;

import java.nio.file.Path;

@Value
public class TodoEntry {

    PathChangeType type;
    Path changedFile;
    Path watchedPath;

}
