package at.meks.backupclientserver.context.backup.adapter.persistence;

import at.meks.backupclientserver.context.backup.model.BackupedFile;
import at.meks.backupclientserver.context.backup.model.BackupedFileRepository;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Hashtable;
import java.util.Optional;

@Named
@Singleton
@Slf4j
public class EmptyBackupedFileRepository implements BackupedFileRepository {

    //TODO real persistence implementation

    private Hashtable<String, BackupedFile> persistentMap = new Hashtable<>();

    @Override
    public Optional<BackupedFile> findById(String id) {
        log.info("getting file with id {}", id);
        return Optional.ofNullable(persistentMap.get(id));
    }

    @Override
    public void save(BackupedFile backupedFile) {
        log.info("persisting file with id {}", backupedFile.id());
        persistentMap.put(backupedFile.id(), backupedFile);
    }

    @Override
    public void update(BackupedFile backupedFile) {
        persistentMap.put(backupedFile.id(), backupedFile);
    }
}
