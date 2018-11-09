package at.meks.backupclientserver.client;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.nio.file.Path;
import java.util.logging.Level;

public class BackupClientApplication {

    @Inject
    private ApplicationConfig config;

    @Inject
    private FileWatcher fileWatcher;

    @Inject
    private FileChangeHandlerImpl fileChangeHandler;

    @Inject
    private StartupBackuper startupBackuper;

    public static void main(String[] args) {
        java.util.logging.Logger.getGlobal().setLevel(Level.INFO);
        Injector injector = Guice.createInjector();
        BackupClientApplication application = injector.getInstance(BackupClientApplication.class);
        application.run();
    }

    private void run() {
        Path[] pathesToWatch = config.getBackupedDirs();
        fileWatcher.setOnChangeConsumer(fileChangeHandler);
        fileWatcher.setPathsToWatch(pathesToWatch);
        fileWatcher.startWatching();
        startupBackuper.backupIfNecessary(pathesToWatch);
    }

}
