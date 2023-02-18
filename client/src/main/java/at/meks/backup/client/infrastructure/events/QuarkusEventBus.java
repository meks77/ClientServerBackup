package at.meks.backup.client.infrastructure.events;

import at.meks.backup.client.model.Events;
import at.meks.backup.client.model.FileEventListener;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

//TODO implementation of the event bus
@Singleton
@Slf4j
public class QuarkusEventBus implements Events {


    static final String BACKUP_QUEUE = "backup";
    static final String STATUS_CHECK_QUEUE = "statusCheck";

    @Inject
    EventBus eventBus;
    private final Set<FileEventListener> listeners = new HashSet<>();

    @Override
    public void fireFileChanged(FileChangedEvent event) {
        log.info("fire event");
        eventBus.send(STATUS_CHECK_QUEUE, event);
    }

    @Override
    public void fireFileNeedsBackup(FileNeedsBackupEvent event) {
        log.info("fire event");
        eventBus.send(BACKUP_QUEUE, event);
    }

    @Override
    public void register(FileEventListener fileEventListener) {
        listeners.add(fileEventListener);
    }

    @ConsumeEvent(BACKUP_QUEUE)
    void onBackup(FileNeedsBackupEvent event) {
        listeners.forEach(l -> l.onFileNeedsBackup(event));
    }

    @ConsumeEvent(STATUS_CHECK_QUEUE)
    void onBackup(FileChangedEvent event) {
        listeners.forEach(l -> l.onFileChanged(event));
    }

    public void deregisterAll() {
        listeners.clear();
    }
}
