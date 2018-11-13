package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import com.google.inject.Inject;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

class StartupBackuper {

    @Inject
    private BackupManager backupManager;

    @Inject
    private ErrorReporter errorReporter;

    void backupIfNecessary(Path[] paths) {
        Thread initialBackupThread = new Thread(() ->
        Stream.of(paths).forEach(path -> walkThroughDirectoryAndBackupFiles(path, path)));
        initialBackupThread.setDaemon(true);
        initialBackupThread.setPriority(Thread.MIN_PRIORITY);
        initialBackupThread.start();
    }

    private void walkThroughDirectoryAndBackupFiles(Path directory, Path backupSetPath) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)){
            directoryStream.forEach(child -> {
                if (child.toFile().isDirectory()) {
                    walkThroughDirectoryAndBackupFiles(child, backupSetPath);
                } else {
                    backupManager.addForBackup(new TodoEntry(PathChangeType.MODIFIED, child, backupSetPath));
                }
            });
        } catch (IOException e) {
            errorReporter.reportError("error while doing initial backup", e);
        }
    }
}
