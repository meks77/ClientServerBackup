package at.meks.backup.client.model;

import java.nio.file.Path;

public interface FileService {

    void backup(ClientId clientId, Path file);

    boolean isBackupNecessary(ClientId clientId, Path file, Checksum checksum);
}
