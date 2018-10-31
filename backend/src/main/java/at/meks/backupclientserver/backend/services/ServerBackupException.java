package at.meks.backupclientserver.backend.services;

class ServerBackupException extends RuntimeException {

    ServerBackupException(String message, Exception cause) {
        super(message, cause);
    }

    ServerBackupException(String message) {
        super(message);
    }
}
