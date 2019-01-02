package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class StartupFileVisitor extends SimpleFileVisitor<Path> {

    private BackupManager backupManager;
    private final Path backupSetPath;
    private final ErrorReporter errorReporter;

    StartupFileVisitor(BackupManager backupManager, Path backupSetPath, ErrorReporter errorReporter) {
        this.backupManager = backupManager;
        this.backupSetPath = backupSetPath;
        this.errorReporter = errorReporter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        backupManager.addForBackup(new TodoEntry(PathChangeType.MODIFIED, file, backupSetPath));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        errorReporter.reportError("error while doing initial backup", exc);
        return FileVisitResult.SKIP_SUBTREE;
    }
}
