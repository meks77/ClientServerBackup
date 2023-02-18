package at.meks.backup.client.application.file;

import io.micrometer.core.annotation.Counted;
import io.quarkus.vertx.ConsumeEvent;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class FileNeedsBackupListener {

    @ConsumeEvent(Events.BACKUP_QUEUE)
    @Counted
    void fileNeedsBackup(FileNeedsBackupEvent event) {
        log.info("start backup {}", event.file());
    }

}
