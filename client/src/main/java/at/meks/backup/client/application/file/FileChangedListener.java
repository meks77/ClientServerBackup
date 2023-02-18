package at.meks.backup.client.application.file;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.nio.file.Path;

@Slf4j
public class FileChangedListener {

    @Inject
    Events events;

    void fireEvent(Path file) {
        log.info("fire event");
        events.fireFileNeedsBackup(file);
    }
}
