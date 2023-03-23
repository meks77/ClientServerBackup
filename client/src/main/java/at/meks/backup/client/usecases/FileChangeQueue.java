package at.meks.backup.client.usecases;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class FileChangeQueue {

    private final Map<Path, LocalDateTime> fileChanges = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void add(Path path) {
        lock.lock();
        try {
            fileChanges.put(path, LocalDateTime.now());
        } finally {
            lock.unlock();
        }
    }

    public Path next() {
        Optional<Path> nextAvailableEvent = Optional.empty();
        while(nextAvailableEvent.isEmpty()) {
            nextAvailableEvent = nextAvailableEvent();
            if (nextAvailableEvent.isEmpty()) {
                try {
                    // TODO: maybe with Conditions a better solution can be build
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return nextAvailableEvent.get();
    }

    private Optional<Path> nextAvailableEvent() {
        lock.lock();
        try {
            Optional<Path> result = fileChanges.entrySet().stream()
                    .filter(this::lastUpdateIsOlderThan50Millis)
                    .map(Map.Entry::getKey)
                    .findFirst();
            result.ifPresent(fileChanges::remove);
            return result;
        } finally {
            lock.unlock();
        }
    }

    private boolean lastUpdateIsOlderThan50Millis(Map.Entry<Path, LocalDateTime> entry) {
        return entry.getValue().isBefore(LocalDateTime.now().minus(50, ChronoUnit.MILLIS));
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return this.fileChanges.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}
