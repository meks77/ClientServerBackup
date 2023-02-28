package at.meks.backup.client.infrastructure.context.quarkus;

import at.meks.backup.client.application.Start;
import at.meks.backup.client.model.Config;
import at.meks.backup.client.model.Events;
import at.meks.backup.client.model.FileEventListener;
import at.meks.backup.client.model.FileService;
import at.meks.backup.client.usecases.DirectoryScanner;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class CdiProducer {

    @Produces
    @ApplicationScoped
    Start startApplication(Config config, QuarkusExit exitAction, Events events, DirectoryScanner directoryScanner, FileEventListener fileEventListener) {
        return new Start(config, exitAction, events, directoryScanner, fileEventListener);
    }

    @Produces
    @ApplicationScoped
    DirectoryScanner directoryScanner(Config config, Events events) {
        return new DirectoryScanner(config, events);
    }

    @Produces
    @ApplicationScoped
    FileEventListener fileEventListener(FileService fileService, Config config) {
        return new FileEventListener(fileService, config);
    }

}
