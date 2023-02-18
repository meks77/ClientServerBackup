package at.meks.backup.client.application;

import io.micrometer.core.annotation.Counted;
import io.quarkus.vertx.ConsumeEvent;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

@Slf4j
@ApplicationScoped
public class FileNeedsBackupListener {

    @ConsumeEvent("backup")
    @Counted
    void fileNeedsBackup(FileNeedsBackupEvent event) {
        log.info("start backup {}", event.file());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("finished backup {}", event.file());
    }

}
