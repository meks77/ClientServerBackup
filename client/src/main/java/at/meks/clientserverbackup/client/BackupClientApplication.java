package at.meks.clientserverbackup.client;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.logging.Level;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class BackupClientApplication {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Inject
    private ApplicationConfig config;

    @Inject
    private BackupManager backupManager;

    @Inject
    private FileWatcher fileWatcher;

    public static void main(String[] args) {
        java.util.logging.Logger.getGlobal().setLevel(Level.INFO);
        Injector injector = Guice.createInjector();
        BackupClientApplication starter = injector.getInstance(BackupClientApplication.class);
        starter.run();
    }

    private void run() {
        Path[] pathesToWatch = config.getBackupedDirs();
        fileWatcher.setOnChangeConsumer(this::fileChanged);
        fileWatcher.setPathsToWatch(pathesToWatch);
        fileWatcher.startWatching();
    }

    private void fileChanged(WatchEvent.Kind kind, Path path) {
        if (kind == ENTRY_CREATE) {
            backupManager.created(path);
        } else if (kind == ENTRY_MODIFY) {
            backupManager.modified(path);
        } else if (kind == ENTRY_DELETE) {
            backupManager.deleted(path);
        } else {
            logger.error("Unknown WatchEvent.Kind: {}", kind);
        }
    }
}
