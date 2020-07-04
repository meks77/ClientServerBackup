package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Singleton
public class StartupBackuper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    BackupManager backupManager;

    @Inject
    ErrorReporter errorReporter;

    @Inject
    FileExcludeService excludeService;

    public void backupIfNecessary(Path[] paths) {
        Thread initialBackupThread = new Thread(() ->
                Stream.of(paths).forEach(this::walkThroughDirectoryAndBackupFiles));
        initialBackupThread.setDaemon(true);
        initialBackupThread.setName("starupBackuperThread");
        initialBackupThread.setPriority(Thread.MIN_PRIORITY);
        initialBackupThread.start();
    }

    private void walkThroughDirectoryAndBackupFiles(Path backupSetPath) {
        logger.debug("check directory {} for backup", backupSetPath);
        StartupFileVisitor visitor = new StartupFileVisitor(backupManager, errorReporter, excludeService);
        try {
            Files.walkFileTree(backupSetPath, visitor);
        } catch (Exception e) {
            errorReporter.reportError("error while do initial backup for backupset " + backupSetPath, e);
        }
    }

}
