package at.meks.backupclientserver.infrastructure;

import at.meks.backupclientserver.context.possibleactions.ManagedFile;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryGateway;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class AxonProducer {

    @Produces
    @ApplicationScoped
    CommandGateway produceCommandGateway(Configuration configuration) {
        return configuration.commandGateway();
    }

    @Produces
    @ApplicationScoped
    QueryGateway produceQueryGateway(Configuration configuration) {
        return configuration.queryGateway();
    }

    @Produces
    @ApplicationScoped
    Repository<ManagedFile> produceBackupedFileRepository(Configuration configuration) {
        return configuration.repository(ManagedFile.class);
    }

}
