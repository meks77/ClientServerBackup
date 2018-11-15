package at.meks.backupclientserver.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
class BackupConfiguration {

    @Value("${application.root.dir}")
    private String applicationRoot;

    Path getApplicationRootDirectory() {
        if (applicationRoot == null) {
            throw new ServerBackupException("applicationRoot directory is not set");
        }
        Path backupDir = Paths.get(applicationRoot, ".clientServerBackup", "backups");
        backupDir.toFile().mkdirs();
        return backupDir;
    }

}
