package at.meks.clientserverbackup.client;

public class ClientBackupException extends RuntimeException {

    ClientBackupException(String message) {
        super(message);
    }

    public ClientBackupException(String message, Exception cause) {
        super(message, cause);
    }
}
