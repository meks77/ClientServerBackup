package at.meks.backup.server.domain.model.file;


import at.meks.backup.server.domain.model.directory.BackupedDirectoryId;

record FilePath (Path pathWithinDirectory, BackupedDirectoryId directoryId) { }
