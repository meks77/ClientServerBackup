package at.meks.backupclientserver.client.filechangehandler;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.SystemService;
import at.meks.backupclientserver.client.backup.model.Client;
import at.meks.backupclientserver.client.backup.model.EventType;
import at.meks.backupclientserver.client.backup.model.FileChangedEvent;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Optional;

@Singleton
@Slf4j
public class FileChangeHandlerImpl implements FileChangeHandler {

    @Inject
    ErrorReporter errorReporter;

    @Inject
    SystemService systemService;

    @Inject
    EventBus eventBus;

    @Inject
    Vertx vertx;

    @Override
    public void fileChanged(WatchEvent.Kind<?> kind, Path changedFile) {
        try {
            if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                eventBus.publish("backup", new FileChangedEvent(new Client(systemService.getHostname()),
                        changedFile, EventType.DELETED));
            } else if (changedFile.toFile().isFile()) {
                getEventType(kind).ifPresent(eventType ->
                        vertx.setTimer(500, id -> {
                            log.info("publish event");
                        eventBus.publish("backup", new FileChangedEvent(new Client(systemService.getHostname()),
                                changedFile, eventType));
                        }));
            } else if (changedFile.toFile().isDirectory()) {
                addDirectoryToQueue(kind, changedFile);
            }
        } catch (Exception e) {
            String message = "error while adding file change to queue. kind: " +
                    kind + " changedFile: " + changedFile;
            errorReporter.reportError(message, e);
        }
    }

    private void addDirectoryToQueue(WatchEvent.Kind<?> kind, Path changedDirectory) throws IOException {
        if (!kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(changedDirectory)) {
                directoryStream.forEach(path -> fileChanged(kind, path));
            }
        }
    }

    private Optional<EventType> getEventType(WatchEvent.Kind<?> kind) {
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            return Optional.of(EventType.CREATED);
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            return Optional.of(EventType.DELETED);
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            return Optional.of(EventType.MODIFIED);
        } else {
            errorReporter.reportError("unknown WatchEvent.Kind " + kind,
                    new ClientBackupException("unknown WatchEvent.Kind " + kind));
            return Optional.empty();
        }
    }

}
