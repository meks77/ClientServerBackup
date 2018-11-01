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
    private FileChangeHandlerImpl fileChangeHandler;

    public static void main(String[] args) throws InterruptedException {
        java.util.logging.Logger.getGlobal().setLevel(Level.INFO);
        Injector injector = Guice.createInjector();
        BackupClientApplication application = injector.getInstance(BackupClientApplication.class);
        application.run();
    }

    private void run() throws InterruptedException {
        Path[] pathesToWatch = config.getBackupedDirs();
        fileWatcher.setOnChangeConsumer(fileChangeHandler);
        fileWatcher.setPathsToWatch(pathesToWatch);
        fileWatcher.startWatching();
        //TODO verify each file if backup is necessary and schedule for backup if necessary
        fileWatcher.join();
    }

}
