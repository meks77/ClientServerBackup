package at.meks.backupclientserver.common.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackupCommandArgs {

    String relativePathUplodadedFile;
    String hostName;
    String[] relativePath;
    String backupedPath;
    String fileName;
    String clientId;

}
