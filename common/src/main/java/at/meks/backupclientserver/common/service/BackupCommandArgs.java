package at.meks.backupclientserver.common.service;

import lombok.Value;

@Value
public class BackupCommandArgs {

    String relativePathUplodadedFile;
    String hostName;
    String[] relativePath;
    String backupedPath;
    String fileName;

}
