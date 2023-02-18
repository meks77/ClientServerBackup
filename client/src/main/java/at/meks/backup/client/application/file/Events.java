package at.meks.backup.client.application.file;

import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;

@ApplicationScoped
@Slf4j
public class Events {


    static final String BACKUP_QUEUE = "backup";
    static final String STATUS_CHECK_QUEUE = "statusCheck";

    @Inject
    EventBus eventBus;

    void fireFileNeedsBackupCheck(Path file) {
        log.info("fire event");
        eventBus.send(BACKUP_QUEUE, new FileNeedsBackupEvent(file));
    }

    void fireFileNeedsBackup(Path file) {
        log.info("fire event");
        eventBus.send(STATUS_CHECK_QUEUE, new FileNeedsBackupEvent(file));
    }

}
