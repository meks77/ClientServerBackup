package at.meks.clientserverbackup.client;

class ClientBackupException extends RuntimeException {

    ClientBackupException(String message, Exception cause) {
        super(message, cause);
    }
}
