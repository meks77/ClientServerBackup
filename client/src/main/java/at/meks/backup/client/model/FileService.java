package at.meks.backup.client.model;

import at.meks.backup.shared.model.Checksum;

import java.nio.file.Path;

public interface FileService {

    void backup(ClientId clientId, Path file);

    boolean isBackupNecessary(ClientId clientId, Path file, Checksum checksum);
}
