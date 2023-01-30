package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.BackupClientId;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static at.meks.backup.server.domain.model.directory.BackupedDirectory.newDirectoryForBackup;
import static org.assertj.core.api.Assertions.assertThat;

public class BackupedDirectoryTest {

    @Test
    void newDirectoryIsCreated() {
        BackupClientId clientId = new BackupClientId("whateverId");
        BackupedDirectory backupedDirectory = newDirectoryForBackup(clientId, new PathOnClient(Path.of("home", "bwayne", "pictures")));

        assertThat(backupedDirectory)
                .isNotNull()
                .extracting(BackupedDirectory::id)
                .isEqualTo(new BackupedDirectoryId("whateverId:home/bwayne/pictures"));
    }
}
