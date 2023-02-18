package at.meks.backup.client.model;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
public class DirectoryScanner implements ScanDirectoryCommandListener {

    private final Config config;
    private final Events events;

    public DirectoryScanner(Config config, Events events) {
        this.config = config;
        this.events = events;
    }

    @Override
    public void scanDirectories() {
        log.info("start scanning directories");
        try {
            for (DirectoryForBackup folder : config.backupedDirectories()) {
                log.info("start scanning folder " + folder.file());
                try (Stream<Path> pathStream = Files.walk(folder.file(), Integer.MAX_VALUE)) {
                        pathStream.filter(Files::isRegularFile)
                            .map(Events.FileChangedEvent::new)
                            .forEach(events::fireFileChanged);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
