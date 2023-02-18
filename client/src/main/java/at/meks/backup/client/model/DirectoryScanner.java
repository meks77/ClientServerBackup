package at.meks.backup.client.model;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.stream.Stream;

import static com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

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
                listenToChanges(folder);
                fireChangedEventForEachFile(folder);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fireChangedEventForEachFile(DirectoryForBackup folder) {
        try (Stream<Path> pathStream = Files.walk(folder.file(), Integer.MAX_VALUE)) {
                pathStream.filter(Files::isRegularFile)
                    .map(Events.FileChangedEvent::new)
                    .forEach(events::fireFileChanged);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void listenToChanges(DirectoryForBackup folder) throws IOException {
        // TODO Listen to changes and fire event
        WatchKey watchKey = folder.file().register(
                FileSystems.getDefault().newWatchService(),
                new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY},
                FILE_TREE);
    }

}
