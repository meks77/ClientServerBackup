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

    static final String STATUS_CHECK_QUEUE = "statusCheck";
    private static final String SCAN_DIRECTORIES_QUEUE = "scanDirectories";

    private final Set<FileEventListener> fileEventListeners = new HashSet<>();
    private final Set<ScanDirectoryCommandListener> scanDirectoryCommandListeners = new HashSet<>();

    @Inject
    EventBus eventBus;

    @Override
    public void fireScanDirectories() {
        log.trace("fire scan directories command");
        eventBus.publish(SCAN_DIRECTORIES_QUEUE, new ScanDirectoriesCommand());
    }

    @Override
    public void fireFileChanged(FileChangedEvent event) {
        log.trace("fire " + event);
        eventBus.publish(STATUS_CHECK_QUEUE, event);
    }

    @Override
    public void register(FileEventListener fileEventListener) {
        fileEventListeners.add(fileEventListener);
    }

    @Override
    public void register(ScanDirectoryCommandListener scanDirectoryCommandListener) {
        scanDirectoryCommandListeners.add(scanDirectoryCommandListener);
    }

    @ConsumeEvent(STATUS_CHECK_QUEUE)
    void onFileChanged(FileChangedEvent event) {
        log.trace("received " + event);
        fileEventListeners.forEach(l -> l.onFileChanged(event));
    }

    @ConsumeEvent(SCAN_DIRECTORIES_QUEUE)
    void onScanDirectories(ScanDirectoriesCommand command) {
        log.trace("received " + command);
        scanDirectoryCommandListeners.forEach(ScanDirectoryCommandListener::scanDirectories);
    }

    public void deregisterAll() {
        fileEventListeners.clear();
    }

}
