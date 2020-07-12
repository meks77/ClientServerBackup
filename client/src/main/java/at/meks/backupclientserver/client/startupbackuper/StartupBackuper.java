package at.meks.backupclientserver.client.startupbackuper;

import at.meks.backupclientserver.client.ApplicationConfig;
import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.infrastructure.SystemService;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Singleton
public class StartupBackuper {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    SystemService systemService;

    @Inject
    ErrorReporter errorReporter;

    @Inject
    FileExcludeService excludeService;

    @Inject
    EventBus eventBus;

    @Inject
    ApplicationConfig config;

    void onStart(@Observes StartupEvent ev) {
        backupIfNecessary();
    }

    private void backupIfNecessary() {
        Stream.of(config.getBackupedDirs()).forEach(this::walkThroughDirectoryAndBackupFiles);
    }

    private void walkThroughDirectoryAndBackupFiles(Path backupSetPath) {
        logger.debug("check directory {} for backup", backupSetPath);
        StartupFileVisitor visitor = new StartupFileVisitor(systemService.getHostname(), eventBus, errorReporter, excludeService);
        try {
            Files.walkFileTree(backupSetPath, visitor);
        } catch (Exception e) {
            errorReporter.reportError("error while do initial backup for backupset " + backupSetPath, e);
        }
    }

}
