package at.meks.backup.client.usecases;

import at.meks.backup.client.model.DirectoryForBackup;
import at.meks.backup.client.model.Events;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Slf4j
public class FileChangeListener {

    private final Events events;

    private final WatchKeyRegistry watchKeyRegistry;

    private final FileChangeQueue fileChangeQueue;

    public FileChangeListener(Events events, WatchKeyRegistry watchKeyRegistry, FileChangeQueue fileChangeQueue) {
        this.events = events;
        this.watchKeyRegistry = watchKeyRegistry;
        this.fileChangeQueue = fileChangeQueue;
    }

    void listenToChangesAsync(DirectoryForBackup folder) {
        log.debug("start listenToChanges");
        startThread(() -> listenToFilesystemWrappingException(folder));
        startThread(this::backupFilesFromQueue);

    }

    private void startThread(Runnable process) {
        Thread thread = new Thread(process);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void listenToFilesystemWrappingException(DirectoryForBackup folder) {
        try {
            listenToChangesThrowingException(folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void listenToChangesThrowingException(DirectoryForBackup folder) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        registerDirectoryForChanges(folder, watchService);
        listenToAllChanges(watchService);
    }

    private void registerDirectoryForChanges(DirectoryForBackup folder, WatchService watchService) throws IOException {
        WatchKey watchKey = folder.file().register(
                watchService,
                ENTRY_CREATE, ENTRY_MODIFY
        );
        watchKeyRegistry.add(watchKey, folder.file());
        log.debug("attached watcher for directory {}", folder.file());
    }

    private void listenToAllChanges(WatchService watchService) {
        new Thread(() -> {
            while (true) {
                try {
                    WatchKey key = watchService.take();
                    changedFile(key)
                            .filter(Files::isRegularFile)
                            .ifPresent(fileChangeQueue::add);
                    key.reset();
                } catch (InterruptedException e) {
                    log.warn("Watching for file changes was interrupted", e);
                    break;
                }
            }
        }).start();
    }

    private Optional<Path> changedFile(WatchKey key) {
        List<WatchEvent<?>> watchEvents = key.pollEvents();
        return cast(watchEvents)
                .map(event -> watchKeyRegistry.directory(key).resolve(event.context()));
    }

    @SuppressWarnings("unchecked")
    private Optional<WatchEvent<Path>> cast(List<WatchEvent<?>> watchEvents) {
        if (watchEvents.isEmpty()) {
            return Optional.empty();
        }
        WatchEvent<?> event = watchEvents.get(0);
        log.debug("got event of kind {} for {}", event.kind(), event.context());
        return Optional.of((WatchEvent<Path>) event);
    }

    private void backupFilesFromQueue() {
        do {
            Path changedFile = fileChangeQueue.next();
            log.debug("fire file changed: {}", changedFile);
            events.fireFileChanged(new Events.FileChangedEvent(changedFile));
        } while (true);
    }

}
