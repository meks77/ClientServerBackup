package at.meks.backup.client.infrastructure.context.quarkus;

import at.meks.backup.client.application.Start;
import at.meks.backup.client.infrastructure.config.QuarkusConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class CdiProducer {

    @Produces
    @ApplicationScoped
    Start startApplication(QuarkusConfig config, QuarkusExit exitAction) {
        return new Start(config, exitAction);
    }

}
