package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.file.version.Content;
import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class BackupedFileService {

    private final BackupedFileRepository fileRepository;
    private final VersionRepository versionRepository;
    private final UtcClock clock;

    BackupedFileService(BackupedFileRepository fileRepository, VersionRepository versionRepository, UtcClock clock) {
        this.fileRepository = fileRepository;
        this.versionRepository = versionRepository;
        this.clock = clock;
    }

    public void backup(FileId fileId, Content fileContent) {
        BackupedFile backupedFile = fileRepository.get(fileId)
                .orElseGet(() -> fileRepository.add(BackupedFile.newFileForBackup(fileId)));
        if (!fileContent.hash().equals(backupedFile.latestVersionHash().orElse(null))) {
            versionRepository.add(Version.newVersion(fileId, new BackupTime(clock.now()), fileContent));
        }
    }

    public boolean isBackupNecessarry(FileId fileId, Checksum checksum) {
        return fileRepository.get(fileId)
                .map(BackupedFile::latestVersionHash)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(file -> !file.equals(checksum))
                .orElse(true);
    }
}
