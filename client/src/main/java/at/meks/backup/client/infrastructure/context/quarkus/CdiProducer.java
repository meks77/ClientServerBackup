package at.meks.backup.client.infrastructure.context.quarkus;

import at.meks.backup.client.application.Start;
import at.meks.backup.client.infrastructure.config.QuarkusConfig;
import at.meks.backup.client.infrastructure.events.QuarkusEventBus;
import at.meks.backup.client.model.Events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

//TODO: tests for Producer
public class CdiProducer {

    @Inject
    QuarkusEventBus eventBus;

    @Produces
    Events eventsBus() {
        return  eventBus;
    }

    @Produces
    @ApplicationScoped
    Start startApplication(QuarkusConfig config, QuarkusExit exitAction) {
        return new Start(config, exitAction);
    }

}
