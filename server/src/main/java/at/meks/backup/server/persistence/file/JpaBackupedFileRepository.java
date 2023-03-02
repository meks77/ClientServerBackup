package at.meks.backup.server.persistence.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.BackupedFile;
import at.meks.backup.server.domain.model.file.BackupedFileRepository;
import at.meks.backup.server.domain.model.file.FileId;
import at.meks.backup.shared.model.Checksum;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class JpaBackupedFileRepository implements BackupedFileRepository {

    @Override
    public Optional<BackupedFile> get(FileId fileId) {
        return findById(fileId)
                .map(this::toDomainEntity);
    }

    private static Optional<BackupedFileEntity> findById(FileId fileId) {
        return BackupedFileEntity.findByFileId(fileId);
    }

    private BackupedFile toDomainEntity(BackupedFileEntity dbEntity) {
        BackupedFile backupedFile = BackupedFile.newFileForBackup(
                FileId.idFor(ClientId.existingId(dbEntity.clientId), new PathOnClient(Path.of(dbEntity.pathOnClient))));
        backupedFile.versionWasBackedup(new Checksum(dbEntity.latestVersionChecksum));
        return backupedFile;
    }

    @Override
    public BackupedFile add(BackupedFile newFileForBackup) {
        BackupedFileEntity entity = toDbEntity(newFileForBackup);
        entity.persist();
        return newFileForBackup;
    }

    private BackupedFileEntity toDbEntity(BackupedFile newFileForBackup) {
        BackupedFileEntity entity = new BackupedFileEntity();
        entity.id = UUID.randomUUID().toString();
        entity.clientId = newFileForBackup.id().clientId().text();
        entity.pathOnClient = newFileForBackup.id().pathOnClient().asText();
        newFileForBackup.latestVersionChecksum()
                .ifPresent(checksum -> entity.latestVersionChecksum = checksum.hash());
        return entity;
    }

    @Override
    public void set(BackupedFile fileForBackup) {
        findById(fileForBackup.id())
                .ifPresentOrElse(
                        dbEntity -> dbEntity.latestVersionChecksum =
                                fileForBackup.latestVersionChecksum().map(Checksum::hash).orElse(null),
                        () -> add(fileForBackup)
                );
    }

    @Override
    public List<BackupedFile> find(ClientId clientId) {
        log.info("searchive files for client {}", clientId.text());
        return BackupedFileEntity.<BackupedFileEntity>list("clientId", clientId.text()).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }
}
