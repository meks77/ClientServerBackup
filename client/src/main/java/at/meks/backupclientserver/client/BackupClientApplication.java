package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.filechangehandler.FileChangeHandlerImpl;
import at.meks.backupclientserver.client.filewatcher.FileWatcher;
import at.meks.backupclientserver.client.startupbackuper.StartupBackuper;
import at.meks.validation.result.ValidationException;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;


import javax.inject.Inject;
import java.nio.file.Path;
import java.util.logging.Level;

@QuarkusMain
public class BackupClientApplication implements QuarkusApplication {

    @Inject
    ApplicationConfig config;

    @Inject
    FileWatcher fileWatcher;

    @Inject
    FileChangeHandlerImpl fileChangeHandler;

    @Inject
    HeartBeatReporter heartBeatReporter;

    @Inject
    StartupBackuper startupBackuper;

    @Override
    public int run(String... args) throws Exception {
        config.validate();
        heartBeatReporter.startHeartbeatReporting();
        Path[] pathesToWatch = config.getBackupedDirs();
        fileWatcher.setOnChangeConsumer(fileChangeHandler);
        fileWatcher.setPathsToWatch(pathesToWatch);
        fileWatcher.startWatching();
        startupBackuper.backupIfNecessary(pathesToWatch);
        Quarkus.waitForExit();
        return 0;
    }

}
