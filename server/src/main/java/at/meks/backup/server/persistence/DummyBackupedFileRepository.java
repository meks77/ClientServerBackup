package at.meks.backup.server.persistence;

import at.meks.backup.server.domain.model.file.BackupedFile;
import at.meks.backup.server.domain.model.file.BackupedFileRepository;
import at.meks.backup.server.domain.model.file.FileId;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class DummyBackupedFileRepository implements BackupedFileRepository {
    @Override
    public Optional<BackupedFile> get(FileId fileId) {
        return Optional.empty();
    }

    @Override
    public BackupedFile add(BackupedFile newFileForBackup) {
        return null;
    }
}
