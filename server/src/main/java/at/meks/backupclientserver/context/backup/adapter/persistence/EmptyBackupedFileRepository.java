package at.meks.backupclientserver.context.backup.adapter.persistence;

import at.meks.backupclientserver.context.backup.model.BackupedFile;
import at.meks.backupclientserver.context.backup.model.BackupedFileRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.Hashtable;

@Named
@ApplicationScoped
public class EmptyBackupedFileRepository implements BackupedFileRepository {

    //TODO real persistence implementation

    private Hashtable<String, BackupedFile> persistentMap = new Hashtable<>();

    @Override
    public BackupedFile findById(String id) {
        return persistentMap.get(id);
    }

    @Override
    public void save(BackupedFile backupedFile) {
        persistentMap.put(backupedFile.id(), backupedFile);
    }

    @Override
    public void update(BackupedFile backupedFile) {
        persistentMap.put(backupedFile.id(), backupedFile);
    }
}
