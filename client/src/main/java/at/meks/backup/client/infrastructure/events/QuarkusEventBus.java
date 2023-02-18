package at.meks.backup.client.infrastructure.events;

import at.meks.backup.client.model.Events;
import at.meks.backup.client.model.FileEventListener;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

//TODO implementation of the event bus
@Singleton
@Slf4j
public class QuarkusEventBus implements Events {


    static final String BACKUP_QUEUE = "backup";
    static final String STATUS_CHECK_QUEUE = "statusCheck";

    @Inject
    EventBus eventBus;

    @Override
    public void fireFileNeedsBackupCheck(FileNeedsBackupEvent event) {
        log.info("fire event");
        eventBus.send(BACKUP_QUEUE, event);
    }

    @Override
    public void fireFileNeedsBackup(FileNeedsBackupEvent event) {
        log.info("fire event");
        eventBus.send(STATUS_CHECK_QUEUE, event);
    }

    @Override
    public void register(FileEventListener fileEventListener) {

    }

}
