package at.meks.backup.client.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FileEventListener {

    private final FileService fileService;
    private final Config config;
    private final Events events;

    public void onFileNeedsBackup(Events.BackupCommand event) {
        log.trace("file needs backup {}", event.file());
        fileService.backup(config.clientId(), event.file());
    }

    public void onFileChanged(Events.FileChangedEvent event) {
        log.trace("File needs status check {}", event.file());
        fileService.fireBackupIfNecessary(
                config.clientId(),
                event.file(),
                Checksum.forContentOf(event.file().toUri()),
                file -> events.fireFileNeedsBackup(new Events.BackupCommand(file)));
    }

}
