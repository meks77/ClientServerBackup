package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.filechangehandler.FileChangeHandlerImpl;
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
    private HeartBeatReporter heartBeatReporter;


    @Inject
    private StartupBackuper startupBackuper;

    public static void main(String[] args) {
        java.util.logging.Logger.getGlobal().setLevel(Level.INFO);
        Injector injector = Guice.createInjector();
        BackupClientApplication application = injector.getInstance(BackupClientApplication.class);
        application.run();
    }

    private void run() {
        heartBeatReporter.startHeartbeatReporting();
        Path[] pathesToWatch = config.getBackupedDirs();
        fileWatcher.setOnChangeConsumer(fileChangeHandler);
        fileWatcher.setPathsToWatch(pathesToWatch);
        fileWatcher.startWatching();
        startupBackuper.backupIfNecessary(pathesToWatch);
    }

}
