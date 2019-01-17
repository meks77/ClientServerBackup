package at.meks.backupclientserver.client.filewatcher;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.excludes.FileExcludeService;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

class FileVisitor extends SimpleFileVisitor<Path> {

    private final Consumer<Path> directoryConsumer;
    private final ErrorReporter errorReporter;
    private final FileExcludeService excludeService;


    FileVisitor(Consumer<Path> directoryConsumer, ErrorReporter errorReporter, FileExcludeService excludeService) {
        this.directoryConsumer = directoryConsumer;
        this.errorReporter = errorReporter;
        this.excludeService = excludeService;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (!dir.toFile().canRead() || excludeService.isFileExcludedFromBackup(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        directoryConsumer.accept(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        errorReporter.reportError("error start listening to file changes of " + file, exc);
        return FileVisitResult.CONTINUE;
    }

}
