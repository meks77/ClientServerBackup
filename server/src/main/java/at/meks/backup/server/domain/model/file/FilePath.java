package at.meks.backup.server.domain.model.file;


import at.meks.backup.server.domain.model.directory.DirectoryId;

record FilePath (Path pathWithinDirectory, DirectoryId directoryId) { }
