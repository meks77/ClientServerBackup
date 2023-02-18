package at.meks.backup.client.infrastructure.events;

import at.meks.backup.client.model.Events;
import at.meks.backup.client.model.FileEventListener;
import at.meks.backup.client.model.ScanDirectoryCommandListener;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
@Slf4j
public class QuarkusEventBus implements Events {

    static final String BACKUP_QUEUE = "backup";
    static final String STATUS_CHECK_QUEUE = "statusCheck";

    @Inject
    EventBus eventBus;
    private final Set<FileEventListener> fileEventListeners = new HashSet<>();
    private final Set<ScanDirectoryCommandListener> scanDirectoryCommandListeners = new HashSet<>();

    @Override
    public void fireScanDirectories() {
        eventBus.publish(BACKUP_QUEUE, new ScanDirectoriesCommand());
    }

    @Override
    public void fireFileChanged(FileChangedEvent event) {
        log.info("fire event");
        eventBus.publish(STATUS_CHECK_QUEUE, event);
    }

    @Override
    public void fireFileNeedsBackup(BackupCommand event) {
        log.info("fire event");
        eventBus.publish(BACKUP_QUEUE, event);
    }

    @Override
    public void register(FileEventListener fileEventListener) {
        fileEventListeners.add(fileEventListener);
    }

    @Override
    public void register(ScanDirectoryCommandListener scanDirectoryCommandListener) {
        scanDirectoryCommandListeners.add(scanDirectoryCommandListener);
    }

    @ConsumeEvent(BACKUP_QUEUE)
    void onBackup(BackupCommand command) {
        fileEventListeners.forEach(l -> l.onFileNeedsBackup(command));
    }

    @ConsumeEvent(STATUS_CHECK_QUEUE)
    void onFileChanged(FileChangedEvent event) {
        fileEventListeners.forEach(l -> l.onFileChanged(event));
    }

    @ConsumeEvent(BACKUP_QUEUE)
    void onScanDirectories(ScanDirectoriesCommand command) {
        scanDirectoryCommandListeners.forEach(ScanDirectoryCommandListener::scanDirectories);
    }

    public void deregisterAll() {
        fileEventListeners.clear();
    }

}
