package at.meks.backupclientserver.context.possibleactions;

import at.meks.backupclientserver.api.BackupAction;
import at.meks.backupclientserver.api.FileBackedUp;
import at.meks.backupclientserver.api.FileDeleted;
import at.meks.backupclientserver.api.FileId;
import at.meks.backupclientserver.api.FileReAdded;
import lombok.NoArgsConstructor;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;

import static at.meks.backupclientserver.api.BackupAction.DELETE;
import static at.meks.backupclientserver.api.BackupAction.UPDATE;

@AggregateRoot
@NoArgsConstructor
public class ManagedFile {

    private static final BackupAction[] AVAILABLE_ACTIONS_IF_DELETED = new BackupAction[]{UPDATE};
    private static final BackupAction[] AVAILABLE_ACTIONS_IF_EXISTS = new BackupAction[]{UPDATE, DELETE};

    @AggregateIdentifier(routingKey = "id")
    private FileId fileId;

    private boolean fileDeleted;

    @EventSourcingHandler(payloadType = FileBackedUp.class)
    void onBackup() {
        fileDeleted = false;
    }

    @EventSourcingHandler(payloadType = FileDeleted.class)
    void onDelete() {
        fileDeleted = true;
    }

    @EventSourcingHandler(payloadType = FileReAdded.class)
    void onReadd() {
        fileDeleted = false;
    }

    public BackupAction[] getAvailableActions() {
        if (fileDeleted) {
            return AVAILABLE_ACTIONS_IF_DELETED;
        } else {
            return AVAILABLE_ACTIONS_IF_EXISTS;
        }
    }
}
