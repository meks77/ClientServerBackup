package at.meks.backupclientserver.client;

public class ClientBackupException extends RuntimeException {

    public ClientBackupException(String message) {
        super(message);
    }

    public ClientBackupException(String message, Exception cause) {
        super(message, cause);
    }

    public ClientBackupException(Throwable cause) {
        super(cause);
    }
}
