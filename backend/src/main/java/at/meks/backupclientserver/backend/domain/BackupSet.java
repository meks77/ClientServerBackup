package at.meks.backupclientserver.backend.domain;

public class BackupSet {

    private String clientBackupSetPath;

    private String directoryNameOnServer;

    public String getClientBackupSetPath() {
        return clientBackupSetPath;
    }

    public void setClientBackupSetPath(String clientBackupSetPath) {
        this.clientBackupSetPath = clientBackupSetPath;
    }

    public String getDirectoryNameOnServer() {
        return directoryNameOnServer;
    }

    public void setDirectoryNameOnServer(String directoryNameOnServer) {
        this.directoryNameOnServer = directoryNameOnServer;
    }
}
