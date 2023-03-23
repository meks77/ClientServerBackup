package at.meks.backup.server.domain.model.file.version;

import at.meks.backup.server.domain.model.file.BackupTime;
import at.meks.backup.server.domain.model.file.FileId;

public record Version(VersionId id, FileId fileId, BackupTime backupTime, long size) {
}
