package at.meks.backup.client.model;

import lombok.extern.slf4j.Slf4j;

//TODO implement event handling
@Slf4j
public class FileEventListener {

    void onFileNeedsBackup(Events.FileNeedsBackupEvent event) {
        log.info("file needs backup {}", event.file());
    }

    void onFileChanged(Events.FileChangedEvent event) {
        log.info("File needs status check {}", event.file());
    }


}
