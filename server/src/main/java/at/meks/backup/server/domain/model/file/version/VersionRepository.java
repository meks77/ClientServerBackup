package at.meks.backup.server.domain.model.file.version;

import at.meks.backup.server.domain.model.file.BackupTime;
import at.meks.backup.server.domain.model.file.BackupedFile;

import java.nio.file.Path;

public interface VersionRepository {

    void add(BackupedFile backupedFile, BackupTime backupTime, Path file);

}
