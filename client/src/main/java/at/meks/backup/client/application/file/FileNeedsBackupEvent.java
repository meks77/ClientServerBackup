package at.meks.backup.client.application.file;

import java.nio.file.Path;

public record FileNeedsBackupEvent(Path file) {

}
