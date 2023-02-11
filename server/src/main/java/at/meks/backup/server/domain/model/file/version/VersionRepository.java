package at.meks.backup.server.domain.model.file.version;

import at.meks.backup.server.domain.model.file.BackupTime;
import at.meks.backup.server.domain.model.file.FileId;

import java.nio.file.Path;

public interface VersionRepository {

    Version add(FileId fileId, BackupTime backupTime, Path file);

}
