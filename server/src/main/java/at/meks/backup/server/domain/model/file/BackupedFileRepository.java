package at.meks.backup.server.domain.model.file;

import java.util.Optional;

public interface BackupedFileRepository {
    Optional<BackupedFile> get(BusinessKey businessKey);

    BackupedFile add(BackupedFile newFileForBackup);
}
