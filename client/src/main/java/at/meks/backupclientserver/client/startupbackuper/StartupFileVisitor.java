package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.PathChangeType;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import at.meks.backupclientserver.client.excludes.FileExcludeService;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class StartupFileVisitor extends SimpleFileVisitor<Path> {

    private BackupManager backupManager;
    private final Path backupSetPath;
    private final ErrorReporter errorReporter;
    private final FileExcludeService fileExcludeService;

    StartupFileVisitor(BackupManager backupManager, Path backupSetPath, ErrorReporter errorReporter,
            FileExcludeService fileExcludeService) {
        this.backupManager = backupManager;
        this.backupSetPath = backupSetPath;
        this.errorReporter = errorReporter;
        this.fileExcludeService = fileExcludeService;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (!dir.toFile().canRead() || fileExcludeService.isFileExcludedFromBackup(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        backupManager.addForBackup(new TodoEntry(PathChangeType.MODIFIED, file, backupSetPath));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        errorReporter.reportError("skip backup for directory " + file + " because of error.", exc);
        return FileVisitResult.SKIP_SUBTREE;
    }
}
