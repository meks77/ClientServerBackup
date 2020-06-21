package at.meks.backupclientserver.context.backup.model;

import java.io.InputStream;

public interface BackupedFileRepository {

    BackupedFile findById(String id);

    void save(BackupedFile backupedFile);

    void update(BackupedFile backupedFile);
}
