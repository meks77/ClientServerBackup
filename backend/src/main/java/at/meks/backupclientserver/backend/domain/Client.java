package at.meks.backupclientserver.backend.domain;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.util.Collection;

@Document(collection = "clients", schemaVersion = "1.0")
public class Client {

    @Id
    private String name;

    private String directoryName;

    private Collection<BackupSet> backupSets;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public Collection<BackupSet> getBackupSets() {
        return backupSets;
    }

    public void setBackupSets(Collection<BackupSet> backupSets) {
        this.backupSets = backupSets;
    }
}
