package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static at.meks.backup.server.domain.model.directory.Directory.directoryWasAdded;
import static org.assertj.core.api.Assertions.assertThat;

public class DirectoryTest {

    @Test
    void newDirectoryIsCreated() {
        ClientId clientId = new ClientId("whateverId");
        Directory backupedDirectory = directoryWasAdded(clientId, new PathOnClient(Path.of("home", "bwayne", "pictures")));

        assertThat(backupedDirectory)
                .isNotNull()
                .extracting(Directory::id)
                .isEqualTo(new DirectoryId("whateverId:home/bwayne/pictures"));
    }

}
