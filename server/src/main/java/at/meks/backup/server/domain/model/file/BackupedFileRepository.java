package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;

import java.util.List;
import java.util.Optional;

public interface BackupedFileRepository {
    Optional<BackupedFile> get(FileId fileId);

    BackupedFile add(BackupedFile newFileForBackup);

    void set(BackupedFile backupedFile);

    List<BackupedFile> find(ClientId existingId);

}
