package at.meks.backupclientserver.backend.services;

public class ServerBackupException extends RuntimeException {

    public ServerBackupException(String message, Exception cause) {
        super(message, cause);
    }

    public ServerBackupException(String message) {
        super(message);
    }

    public ServerBackupException(Exception e) {
        super(e);
    }
}
