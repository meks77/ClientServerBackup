package at.meks.backupclientserver.context.possibleactions;

import at.meks.backupclientserver.api.AvailableActionsQuery;
import at.meks.backupclientserver.api.BackupAction;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ApplicationScoped
public class AvailableActionsQueryHandler {

    @Inject
    private Repository<ManagedFile> fileRepository;

    @QueryHandler
    public BackupAction[] getAvailableActions(AvailableActionsQuery query) {
        try {
            return fileRepository.load(query.getFileProperties().getId().getId()).invoke(ManagedFile::getAvailableActions);
        } catch (AggregateNotFoundException exc) {
            return new BackupAction[] {BackupAction.INITIAL_BACKUP};
        }
    }

}
