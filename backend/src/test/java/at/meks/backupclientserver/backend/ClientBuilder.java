package at.meks.backupclientserver.backend;

import at.meks.backupclientserver.backend.domain.BackupSet;
import at.meks.backupclientserver.backend.domain.Client;

import java.util.Collection;
import java.util.Date;

public final class ClientBuilder {
    private String name;
    private String directoryName;
    private Collection<BackupSet> backupSets;
    private Date lastBackupedFileTimestamp;

    private ClientBuilder() {
    }

    public static ClientBuilder aClient() {
        return new ClientBuilder();
    }

    public ClientBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ClientBuilder withDirectoryName(String directoryName) {
        this.directoryName = directoryName;
        return this;
    }

    public ClientBuilder withBackupSets(Collection<BackupSet> backupSets) {
        this.backupSets = backupSets;
        return this;
    }

    public ClientBuilder withLastBackupedFileTimestamp(Date lastBackupedFileTimestamp) {
        this.lastBackupedFileTimestamp = lastBackupedFileTimestamp;
        return this;
    }

    public Client build() {
        Client client = new Client();
        client.setName(name);
        client.setDirectoryName(directoryName);
        client.setBackupSets(backupSets);
        client.setLastBackupedFileTimestamp(lastBackupedFileTimestamp);
        return client;
    }
}
