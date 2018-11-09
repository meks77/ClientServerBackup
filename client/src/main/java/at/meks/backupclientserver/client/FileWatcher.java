package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.filechangehandler.FileChangeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

class FileWatcher {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Path[] pathsToWatch;
    private FileChangeHandler onChangeConsumer;
    private Map<WatchKey, Path> pathMap = new HashMap<>();
    private Thread listenThread;
    private WatchService watchService;


    void setPathsToWatch(Path[] pathsToWatch) {
        this.pathsToWatch = pathsToWatch;
    }

    void setOnChangeConsumer(FileChangeHandler onChangeConsumer) {
        this.onChangeConsumer = onChangeConsumer;
    }

    void startWatching() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            initializeWatching(watchService);
            listenThread = new Thread(() ->  listenToChanges(watchService));
            listenThread.start();
        } catch (IOException e) {
            throw new ClientBackupException("couldn't create watchService", e);
        }
    }

    void stopWatching() {
        listenThread.interrupt();
        try {
            watchService.close();
        } catch (IOException e) {
            throw new ClientBackupException("couldn't close watchService", e);
        }
    }

    private void initializeWatching(WatchService watchService) {
        for (Path path : pathsToWatch) {
            registerDirectory(watchService, path);
        }
    }

    private void registerDirectory(WatchService watchService, Path path) {
        pathMap.put(registerForWatching(watchService, path), path);
        File file = path.toFile();
        if (file.isDirectory()) {
            File[] subDirs = file.listFiles(File::isDirectory);
            if (subDirs != null) {
                Stream.of(subDirs).forEach(file1 -> registerDirectory(watchService, file1.toPath()));
            }
        }
    }

    private void listenToChanges(WatchService watchService) {
        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        logger.info("overflow event for {}", event.context());
                        continue;
                    }
                    logger.info("event context {}", event.context());
                    Path changedPath = (Path) event.context();
                    logger.info("event {} happened for {}", kind, changedPath.getFileName());
                    Path watchedPath = pathMap.get(key);
                    Path absoluteChangedPath = Paths.get(watchedPath.toString(), changedPath.toString());
                    if (absoluteChangedPath.toFile().isDirectory() && kind == ENTRY_CREATE) {
                        logger.info("register for changes for new directory {}", changedPath);
                        registerDirectory(watchService, absoluteChangedPath);
                    }
                    Optional<Path> first = Stream.of(pathsToWatch).filter(watchedPath::startsWith).findFirst();
                    if (!first.isPresent()) {
                        throw new ClientBackupException(
                                format("couldn't find root backuped path. Watched path: %s;%npathsToWatch: %s",
                                        watchedPath, Arrays.toString(pathsToWatch)));
                    }
                    onChangeConsumer.fileChanged(first.get(), kind, absoluteChangedPath);
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            logger.info("listening to file changes was interrupted", e);
        }
    }

    private WatchKey registerForWatching(WatchService watchService, Path changedPath) {
        try {
            return changedPath.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        } catch (IOException e) {
            throw new ClientBackupException(format("Error happened while registering to listen for changes for %s", changedPath), e);
        }
    }

}

