package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;
import lombok.SneakyThrows;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
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
    @Transactional
    public void backup(FileId fileId, Path file) {
        Optional<BackupedFile> backupedFile = fileRepository.get(fileId);
        Checksum checksum = Checksum.forContentOf(file.toUri());
        if (backupedFile.isPresent() && !checksum.equals(backupedFile.get().latestVersionChecksum().orElse(null))) {
            backupedFile.get().versionWasBackedup(checksum);
            fileRepository.set(backupedFile.get());
            versionRepository.add(backupedFile.get(), new BackupTime(clock.now()), file);
        } else if (backupedFile.isEmpty()){
            BackupedFile newFileForBackup = BackupedFile.newFileForBackup(fileId);
            newFileForBackup.versionWasBackedup(checksum);
            fileRepository.add(newFileForBackup);
            versionRepository.add(newFileForBackup, new BackupTime(clock.now()), file);
        }
    }

    @Transactional
    public boolean isBackupNecessarry(FileId fileId, Checksum checksum) {
        return fileRepository.get(fileId)
                .map(BackupedFile::latestVersionChecksum)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(file -> !file.equals(checksum))
                .orElse(true);
    }
}
