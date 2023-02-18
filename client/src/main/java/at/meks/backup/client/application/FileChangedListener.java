package at.meks.backup.client.application;

import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.nio.file.Path;

@Slf4j
public class FileChangedListener {

    @Inject
    EventBus eventBus;

    private void fireEvent(Path file) {
        log.info("fire event");
        eventBus.send("backup", new FileNeedsBackupEvent(file));
    }
}
