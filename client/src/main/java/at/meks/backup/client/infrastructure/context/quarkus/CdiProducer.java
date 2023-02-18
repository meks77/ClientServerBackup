package at.meks.backup.client.infrastructure.context.quarkus;

import at.meks.backup.client.application.Start;
import at.meks.backup.client.infrastructure.config.QuarkusConfig;
import at.meks.backup.client.model.Events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class CdiProducer {

    @Produces
    @ApplicationScoped
    Start startApplication(QuarkusConfig config, QuarkusExit exitAction, Events events) {
        return new Start(config, exitAction, events);
    }

}
