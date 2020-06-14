package at.meks.backupclientserver.context.backup;

import at.meks.backupclientserver.api.BackupFile;
import at.meks.backupclientserver.api.FileBackedUp;
import at.meks.backupclientserver.api.FileId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@AggregateRoot
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ManagedFile {

    @AggregateIdentifier(routingKey = "id")
    private FileId fileId;

    private Path absolutePathToFile;

    @SneakyThrows
    @CommandHandler
    ManagedFile(BackupFile cmd) {
        String uploadedFile = cmd.getUploadedFile();
        final Path pathToUploadedFile = Paths.get(uploadedFile);
        apply(new FileBackedUp(cmd.getFileProperties().getId(), pathToUploadedFile));
    }

    @EventSourcingHandler
    void onFileBackup(FileBackedUp evt) {
        fileId = evt.getFileId();
        absolutePathToFile = evt.getPathToUploadedFile();
    }

}
