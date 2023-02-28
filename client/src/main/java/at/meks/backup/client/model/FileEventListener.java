package at.meks.backup.client.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class FileEventListener {

    private final FileService fileService;
    private final Config config;

    public void onFileChanged(Events.FileChangedEvent event) {
        log.trace("File needs status check {}", event.file());
        if (isBackupNecessary(event.file())) {
            fileService.backup(config.clientId(), event.file());
        }
    }

    private boolean isBackupNecessary(Path file) {
        return fileService.isBackupNecessary(config.clientId(), file, Checksum.forContentOf(file.toUri()));
    }

}
