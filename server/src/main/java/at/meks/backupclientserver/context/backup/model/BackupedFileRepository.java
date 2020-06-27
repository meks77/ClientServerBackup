package at.meks.backupclientserver.context.backup.model;

import java.util.Optional;

public interface BackupedFileRepository {

    Optional<BackupedFile> findById(String id);

    void save(BackupedFile backupedFile);

    void update(BackupedFile backupedFile);
}
