package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.backup.model.Client;
import at.meks.backupclientserver.client.backup.model.EventType;
import at.meks.backupclientserver.client.backup.model.FileChangedEvent;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import io.vertx.core.eventbus.EventBus;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@AllArgsConstructor
public class StartupFileVisitor extends SimpleFileVisitor<Path> {

    private final String clientId;
    private final EventBus eventBus;
    private final ErrorReporter errorReporter;
    private final FileExcludeService fileExcludeService;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (!dir.toFile().canRead() || fileExcludeService.isFileExcludedFromBackup(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        eventBus.publish("backup", new FileChangedEvent(new Client(clientId), file, EventType.MODIFIED));
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        errorReporter.reportError("skip backup for directory " + file + " because of error.", exc);
        return FileVisitResult.SKIP_SUBTREE;
    }
}
