package at.meks.backupclientserver.context.possibleactions;


import at.meks.backupclientserver.api.BackupAction;
import org.junit.jupiter.api.Test;

import static at.meks.backupclientserver.api.BackupAction.DELETE;
import static at.meks.backupclientserver.api.BackupAction.UPDATE;
import static org.assertj.core.api.Assertions.assertThat;

public class ManagedFileTest {

    private ManagedFile managedFile = new ManagedFile();

    @Test
    public void givenNotDeletedFileWhenGetAvailableActions() {
        managedFile.onBackup();
        assertThat(managedFile.getAvailableActions()).containsOnlyOnce(UPDATE, DELETE);
    }

    @Test
    public void givenDeletedFileWhenGetAvailableActions() {
        managedFile.onDelete();
        assertThat(managedFile.getAvailableActions()).containsOnlyOnce(UPDATE);
    }

    @Test
    public void givenReaddedFleWhenGetAvailableActions() {
        managedFile.onDelete();
        managedFile.onReadd();
        assertThat(managedFile.getAvailableActions()).containsOnlyOnce(UPDATE, BackupAction.DELETE);
    }

}