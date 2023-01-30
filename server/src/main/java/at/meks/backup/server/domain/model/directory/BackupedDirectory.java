package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.BackupClientId;

public class BackupedDirectory {

    private final BackupClientId clientId;
    private final PathOnClient path;
    private final BackupedDirectoryId id;

    public static BackupedDirectory newDirectoryForBackup(BackupClientId clientId, PathOnClient path) {
        return new BackupedDirectory(clientId, path);
    }

    private BackupedDirectory(BackupClientId clientId, PathOnClient path) {
        this.clientId = clientId;
        this.path = path;
        id = new BackupedDirectoryId(clientId.id() + ":" + path.asText());
    }

    public BackupedDirectoryId id() {
        return id;
    }
}
