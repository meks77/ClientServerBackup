package at.meks.backup.client.model;

public interface Config {

    ClientId clientId();

    DirectoryForBackup[] backupedDirectories();

}
