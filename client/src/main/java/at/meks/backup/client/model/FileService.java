package at.meks.backup.client.model;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface FileService {

    void backup(ClientId clientId, Path file);

    void fireBackupIfNecessary(ClientId clientId, Path file, Checksum checksum, Consumer<Path> fileConsuberForBackup);

}
