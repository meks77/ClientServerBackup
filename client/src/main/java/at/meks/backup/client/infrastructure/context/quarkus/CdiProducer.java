package at.meks.backup.client.infrastructure.context.quarkus;

import at.meks.backup.client.application.Start;
import at.meks.backup.client.model.Config;
import at.meks.backup.client.model.Events;
import at.meks.backup.client.model.FileEventListener;
import at.meks.backup.client.model.FileService;
import at.meks.backup.client.usecases.BackupEachFileScanner;
import at.meks.backup.client.usecases.DirectoryScanner;
import at.meks.backup.client.usecases.FileChangeListener;
import at.meks.backup.client.usecases.FileChangeQueue;
import at.meks.backup.client.usecases.WatchKeyRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

public class CdiProducer {

    @Produces
    @ApplicationScoped
    Start startApplication(Config config, QuarkusExit exitAction, Events events, DirectoryScanner directoryScanner,
                           FileEventListener fileEventListener) {
        return new Start(config, exitAction, events, directoryScanner, fileEventListener);
    }

    @Produces
    @ApplicationScoped
    BackupEachFileScanner backupEachFileScanner(Events events) {
        return new BackupEachFileScanner(events);
    }

    @Produces
    @Singleton
    FileChangeQueue fileChangeQueue() {
        return new FileChangeQueue();
    }

    @Produces
    @Singleton
    FileChangeListener fileChangeListener(Events events, WatchKeyRegistry watchKeyRegistry, FileChangeQueue fileChangeQueue) {
        return new FileChangeListener(events, watchKeyRegistry, fileChangeQueue);
    }

    @Produces
    @ApplicationScoped
    DirectoryScanner directoryScanner(Config config, BackupEachFileScanner backupEachFileScanner,
                                      FileChangeListener fileChangeListener) {
        return new DirectoryScanner(config, backupEachFileScanner, fileChangeListener);
    }

    @Produces
    @ApplicationScoped
    FileEventListener fileEventListener(FileService fileService, Config config) {
        return new FileEventListener(fileService, config);
    }

    @Produces
    @Singleton
    WatchKeyRegistry watchKeyRegistry() {
        return new WatchKeyRegistry();
    }

}
