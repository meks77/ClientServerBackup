package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientService;
import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionId;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;
import at.meks.backup.shared.model.Checksum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class BackupedFileService {

    private final BackupedFileRepository fileRepository;
    private final VersionRepository versionRepository;
    private final UtcClock clock;
    private final ClientService clientService;

    BackupedFileService(
            BackupedFileRepository fileRepository,
            VersionRepository versionRepository,
            UtcClock clock,
            ClientService clientService) {
        this.fileRepository = fileRepository;
        this.versionRepository = versionRepository;
        this.clock = clock;
        this.clientService = clientService;
    }

    @SneakyThrows
    @Transactional
    public void backup(FileId fileId, Path file) {
        Checksum checksum = Checksum.forContentOf(file.toUri());
        clientService.registerIfNotExists(fileId.clientId());
        BackupedFile backupedFile = getOrCreateFileMetadata(fileId);
        if (isNewVersion(backupedFile, checksum)) {
            long fileSize = Files.size(file);
            Version version = new Version(
                    VersionId.newId(),
                    backupedFile.id(),
                    new BackupTime(clock.now()),
                    fileSize);
            backupedFile.versionWasBackedup(checksum, fileSize);
            fileRepository.set(backupedFile);
            versionRepository.add(version, file);
        }
    }

    private static boolean isNewVersion(BackupedFile backupedFile, Checksum checksum) {
        return backupedFile.latestVersionChecksum().isEmpty() ||
                !backupedFile.latestVersionChecksum().get().equals(checksum);
    }

    private BackupedFile getOrCreateFileMetadata(FileId fileId) {
        Optional<BackupedFile> backupedFile = fileRepository.get(fileId);
        if (backupedFile.isPresent()) {
            fileRepository.set(backupedFile.get());
            return backupedFile.get();
        } else {
            BackupedFile newFileForBackup = BackupedFile.newFileForBackup(fileId);
            return fileRepository.add(newFileForBackup);
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
