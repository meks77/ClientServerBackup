package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class StartupBackuper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private BackupManager backupManager;

    @Inject
    private ErrorReporter errorReporter;

    public void backupIfNecessary(Path[] paths) {
        Thread initialBackupThread = new Thread(() ->
                Stream.of(paths).forEach(path -> walkThroughDirectoryAndBackupFiles(path, path)));
        initialBackupThread.setDaemon(true);
        initialBackupThread.setName("starupBackuperThread");
        initialBackupThread.setPriority(Thread.MIN_PRIORITY);
        initialBackupThread.start();
    }

    private void walkThroughDirectoryAndBackupFiles(Path directory, Path backupSetPath) {
        logger.debug("check directory {} for backup", directory);
        try {
            Files.walkFileTree(backupSetPath, new StartupFileVisitor(backupManager, backupSetPath, errorReporter));
        } catch (Exception e) {
            errorReporter.reportError("error while doing initial backup", e);
        }
    }

}
