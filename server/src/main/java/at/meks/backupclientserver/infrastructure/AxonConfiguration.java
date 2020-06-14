package at.meks.backupclientserver.infrastructure;

import at.meks.backupclientserver.context.possibleactions.AvailableActionsQueryHandler;
import at.meks.backupclientserver.context.possibleactions.ManagedFile;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
class AxonConfiguration {

    @Inject
    AvailableActionsQueryHandler availableActionsQueryHandler;

    @Produces
    Configuration produceConfiguration() {
        return DefaultConfigurer.defaultConfiguration()
                .configureAggregate(ManagedFile.class)
                .configureAggregate(at.meks.backupclientserver.context.backup.ManagedFile.class)
                .registerQueryHandler(configuration -> availableActionsQueryHandler)
                .buildConfiguration();
    }
}
