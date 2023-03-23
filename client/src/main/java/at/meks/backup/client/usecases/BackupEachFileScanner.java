package at.meks.backup.client.usecases;

import at.meks.backup.client.model.DirectoryForBackup;
import at.meks.backup.client.model.Events;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class BackupEachFileScanner {

    private final Events events;

    public BackupEachFileScanner(@NonNull Events events) {
        this.events = events;
    }

    void fireChangedEventForEachFileAsync(DirectoryForBackup folder) {
        Thread thread = new Thread(() -> fireChangedEventForEachFile(folder));
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void fireChangedEventForEachFile(DirectoryForBackup folder) {
        try (Stream<Path> pathStream = Files.walk(folder.file(), Integer.MAX_VALUE)) {
            fireChangedEventForEachFile(pathStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fireChangedEventForEachFile(Stream<Path> pathStream) {
        pathStream
                .filter(Files::isRegularFile)
                .map(Events.FileChangedEvent::new)
                .forEach(events::fireFileChanged);
    }

}
