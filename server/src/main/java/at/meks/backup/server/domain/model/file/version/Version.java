package at.meks.backup.server.domain.model.file.version;

import at.meks.backup.server.domain.model.file.BackupTime;
import at.meks.backup.server.domain.model.file.FileId;

import java.util.UUID;

public record Version(
        VersionId id,
        FileId fileId,
        BackupTime backuptime,
        Content fileContent) {

    public static Version newVersion(FileId id, BackupTime backupTime, Content fileContent) {
        return new Version(new VersionId(UUID.randomUUID()), id, backupTime, fileContent);
    }
}
