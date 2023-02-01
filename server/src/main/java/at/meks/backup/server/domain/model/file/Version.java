package at.meks.backup.server.domain.model.file;

record Version(
        VersionId id,
        BusinessKey businessKey,
        BackupTime backuptime,
        Content fileContent) {
}
