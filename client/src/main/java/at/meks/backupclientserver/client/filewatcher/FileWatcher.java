package at.meks.backupclientserver.client.filewatcher;

import at.meks.backupclientserver.client.ClientBackupException;
import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.FileService;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import at.meks.backupclientserver.client.filechangehandler.FileChangeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.lang.String.format;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Named
@Singleton
public class FileWatcher {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    ErrorReporter errorReporter;

    @Inject
    FileService fileService;

    @Inject
    FileExcludeService fileExcludeService;

    private Path[] pathsToWatch;
    private FileChangeHandler onChangeConsumer;
    private MemoryOptimizedMap pathMap;
    private Thread listenThread;
    private WatchService watchService;

    public void setPathsToWatch(Path[] pathsToWatch) {
        this.pathsToWatch = pathsToWatch;
    }

    public void setOnChangeConsumer(FileChangeHandler onChangeConsumer) {
        this.onChangeConsumer = onChangeConsumer;
    }

    public void startWatching() {
        try {
            fileService.cleanupDirectoriesMapFiles();
            pathMap = new MemoryOptimizedMap(fileService.getDirectoriesMapFile().toFile());
            watchService = FileSystems.getDefault().newWatchService();
            listenThread = new Thread(() ->  listenToChanges(watchService));
            listenThread.setName("fileChangeListener");
            listenThread.start();
            initializeWatching(watchService);
        } catch (Exception e) {
            errorReporter.reportError("couldn't create watchService", e);
            throw new ClientBackupException("couldn't create watchService", e);
        }
    }

    void stopWatching() {
        listenThread.interrupt();
        try {
            watchService.close();
        } catch (IOException e) {
            errorReporter.reportError("couldn't close watchService", e);
            throw new ClientBackupException("couldn't close watchService", e);
        }
    }

    private void initializeWatching(WatchService watchService) {
        for (Path path : pathsToWatch) {
            registerDirectory(watchService, path);
        }
    }

    private void registerDirectory(WatchService watchService, Path path) {
        FileVisitor visitor = new FileVisitor(dir -> pathMap.put(registerForWatching(watchService, dir), dir),
                errorReporter, fileExcludeService);
        try {
            Files.walkFileTree(path, visitor);
        } catch (Exception e) {
            errorReporter.reportError("Couldn't start listening to changes of " + path, e);
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
                    Path absoluteChangedPath = pathMap.get(key).resolve(changedPath);
                    if (absoluteChangedPath.toFile().isDirectory() && kind == ENTRY_CREATE) {
                        logger.info("register for changes for new directory {}", changedPath);
                        registerDirectory(watchService, absoluteChangedPath);
                    }
                    onChangeConsumer.fileChanged(kind, absoluteChangedPath);
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            logger.info("listening to file changes was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            errorReporter.reportError("error while listening to changes", e);
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

