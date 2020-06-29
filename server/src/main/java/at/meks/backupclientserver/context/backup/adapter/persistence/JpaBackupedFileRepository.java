package at.meks.backupclientserver.context.backup.adapter.persistence;

import at.meks.backupclientserver.context.backup.model.BackupedFile;
import at.meks.backupclientserver.context.backup.model.BackupedFileRepository;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

@Named
@Singleton
@Slf4j
public class JpaBackupedFileRepository implements BackupedFileRepository {

    @Inject
    Translator translator;

    @Override
    public Optional<BackupedFile> findById(String id) {
        log.debug("getting file with id {}", id);
        Optional<BackupedFileEntity> entity = BackupedFileEntity.findByIdOptional(id);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        log.debug("found entity {}", entity.get());
        return Optional.of(translator.toDomain(entity.get()));
    }

    @Override
    public void save(BackupedFile backupedFile) {
        log.debug("persisting file with id {}", backupedFile.id());
        BackupedFileEntity.persist(translator.toNewEntity(backupedFile));
    }

    @Override
    public void update(BackupedFile backupedFile) {
        BackupedFileEntity entity = BackupedFileEntity.findById(backupedFile.id());
        for (int i = entity.versions.size(); i < backupedFile.versions().size(); i++) {
            VersionEntity.persist(translator.toNewEntity(backupedFile.versions().get(i), entity));
        }
        for (int i = entity.deletedTimestamps.size(); i < backupedFile.deletedTimestamps().size(); i++) {
            DeletionDate.persist(translator.toNewEntity(backupedFile.deletedTimestamps().get(i), entity));
        }
    }
}
