package at.meks.clientserverbackup.client;

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
    private FileChangeHandler fileChangeHandler;

    public static void main(String[] args) throws InterruptedException {
        java.util.logging.Logger.getGlobal().setLevel(Level.INFO);
        Injector injector = Guice.createInjector();
        BackupClientApplication application = injector.getInstance(BackupClientApplication.class);
        application.run();
    }

    private void run() throws InterruptedException {
        Path[] pathesToWatch = config.getBackupedDirs();
        fileWatcher.setOnChangeConsumer(fileChangeHandler::fileChanged);
        fileWatcher.setPathsToWatch(pathesToWatch);
        fileWatcher.startWatching();
        fileWatcher.join();
    }

}
