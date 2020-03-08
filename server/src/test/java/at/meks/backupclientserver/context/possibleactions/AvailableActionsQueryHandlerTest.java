package at.meks.backupclientserver.context.possibleactions;

import at.meks.backupclientserver.api.AvailableActionsQuery;
import at.meks.backupclientserver.api.BackupAction;
import at.meks.backupclientserver.api.ClientId;
import at.meks.backupclientserver.api.FileProperties;
import at.meks.backupclientserver.api.ManagedPath;
import at.meks.backupclientserver.api.ManagedRootDir;
import io.quarkus.test.junit.QuarkusTest;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class AvailableActionsQueryHandlerTest {

    @Inject
    QueryGateway gateway;

    @Test
    void givenNotBackupedFileReturnsIntialBackup() {
        CompletableFuture<BackupAction[]> query = gateway.query(
                new AvailableActionsQuery(
                        new FileProperties(
                                new ManagedPath(new ManagedRootDir("myUtRoot"), "myUtRelativePath",
                                        new ClientId("myClientId")),
                                "myUtFileName")),
                BackupAction[].class);

        BackupAction[] response = query.join();
        assertThat(response).containsOnlyOnce(BackupAction.INITIAL_BACKUP);
    }

}