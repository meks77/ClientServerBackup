package at.meks.backup.client.model;

import lombok.extern.slf4j.Slf4j;

//TODO implement event handling
@Slf4j
public class FileEventListener {

    public void onFileNeedsBackup(Events.BackupCommand event) {
        log.info("file needs backup {}", event.file());
    }

    public void onFileChanged(Events.FileChangedEvent event) {
        log.info("File needs status check {}", event.file());
    }

}
