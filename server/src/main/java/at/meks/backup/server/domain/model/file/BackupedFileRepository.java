package at.meks.backup.server.domain.model.file;

import java.util.Optional;

public interface BackupedFileRepository {
    Optional<BackupedFile> get(FileId fileId);

    void add(BackupedFile newFileForBackup);
}
