package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;
import lombok.SneakyThrows;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.util.Optional;

@ApplicationScoped
public class BackupedFileService {

    private final BackupedFileRepository fileRepository;
    private final VersionRepository versionRepository;
    private final UtcClock clock;

    BackupedFileService(
            BackupedFileRepository fileRepository,
            VersionRepository versionRepository,
            UtcClock clock) {
        this.fileRepository = fileRepository;
        this.versionRepository = versionRepository;
        this.clock = clock;
    }

    @SneakyThrows
    public void backup(FileId fileId, Path file) {
        BackupedFile backupedFile = fileRepository.get(fileId)
                .orElseGet(() -> fileRepository.add(BackupedFile.newFileForBackup(fileId)));
        Checksum checksum = Checksum.forContentOf(file.toUri());
        if (!checksum.equals(backupedFile.latestVersionChecksum().orElse(null))) {
            versionRepository.add(fileId, new BackupTime(clock.now()), file);
        }
    }

    public boolean isBackupNecessarry(FileId fileId, Checksum checksum) {
        return fileRepository.get(fileId)
                .map(BackupedFile::latestVersionChecksum)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(file -> !file.equals(checksum))
                .orElse(true);
    }
}
