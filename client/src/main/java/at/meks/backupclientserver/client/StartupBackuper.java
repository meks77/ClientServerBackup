package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

class StartupBackuper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private BackupManager backupManager;

    @Inject
    private ErrorReporter errorReporter;

    void backupIfNecessary(Path[] paths) {
        Thread initialBackupThread = new Thread(() ->
                Stream.of(paths).forEach(path -> walkThroughDirectoryAndBackupFiles(path, path)));
        initialBackupThread.setDaemon(true);
        initialBackupThread.setName("starupBackuperThread");
        initialBackupThread.setPriority(Thread.MIN_PRIORITY);
        initialBackupThread.start();
    }

    private void walkThroughDirectoryAndBackupFiles(Path directory, Path backupSetPath) {
        logger.debug("check directory {} for backup", directory);
        if (!Files.isSymbolicLink(directory) && Files.isReadable(directory)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
                directoryStream.forEach(child -> {
                    if (child.toFile().isDirectory()) {
                        walkThroughDirectoryAndBackupFiles(child, backupSetPath);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("adding {} for backup", child);
                        }
                        backupManager.addForBackup(new TodoEntry(PathChangeType.MODIFIED, child, backupSetPath));
                    }
                });
            } catch (Exception e) {
                errorReporter.reportError("error while doing initial backup", e);
            }
        }
    }
}
