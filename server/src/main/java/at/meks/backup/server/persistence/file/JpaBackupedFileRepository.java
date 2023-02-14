package at.meks.backup.server.persistence.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.BackupedFile;
import at.meks.backup.server.domain.model.file.BackupedFileRepository;
import at.meks.backup.server.domain.model.file.Checksum;
import at.meks.backup.server.domain.model.file.FileId;
import io.quarkus.panache.common.Parameters;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JpaBackupedFileRepository implements BackupedFileRepository {

    @Override
    public Optional<BackupedFile> get(FileId fileId) {

        return BackupedFileEntity.<BackupedFileEntity>find(
                        "#BackupedFileEntity.findByFileId",
                        Parameters.with("clientId", fileId.clientId().text())
                                .and("pathOnClient", fileId.pathOnClient().asText()).map())
                .firstResultOptional()
                .map(this::toDomainEntity);
    }

    private BackupedFile toDomainEntity(BackupedFileEntity dbEntity) {
        BackupedFile backupedFile = BackupedFile.newFileForBackup(
                FileId.idFor(ClientId.existingId(dbEntity.clientId), new PathOnClient(Path.of(dbEntity.pathOnClient))));
        backupedFile.versionWasBackedup(new Checksum(dbEntity.latestVersionChecksum));
        return backupedFile;
    }

    @Override
    public BackupedFile add(BackupedFile newFileForBackup) {
        BackupedFileEntity entity = new BackupedFileEntity();
        entity.id = UUID.randomUUID().toString();
        entity.clientId = newFileForBackup.id().clientId().text();
        entity.pathOnClient = newFileForBackup.id().pathOnClient().asText();
        newFileForBackup.latestVersionChecksum()
                .ifPresent(checksum -> entity.latestVersionChecksum = checksum.hash());
        entity.persist();
        return newFileForBackup;
    }

    @Override
    public void set(BackupedFile backupedFile) {

    }

}
