package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static at.meks.backup.server.domain.model.directory.Directory.directoryWasAdded;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DirectoryTest {

    public static final PathOnClient PATH_TO_PICTURES = new PathOnClient(Path.of("home", "bwayne", "pictures"));
    public static final ClientId CLIENT_ID = new ClientId("whateverId");

    @Test
    void newDirectoryIsCreated() {
        Directory backupedDirectory = directoryWasAdded(CLIENT_ID, PATH_TO_PICTURES);

        assertThat(backupedDirectory)
                .isNotNull()
                .extracting(Directory::id)
                .isEqualTo(DirectoryId.idFor(CLIENT_ID, PATH_TO_PICTURES));
    }

    @Test
    void clientIdIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> directoryWasAdded(null, PATH_TO_PICTURES))
                .withMessageContaining("clientId");
    }

    @Test
    void pathIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> directoryWasAdded(CLIENT_ID, null))
                .withMessageContaining("path");
    }

    @Test
    void equalsOnlyForId() {
        Directory left = directoryWasAdded(CLIENT_ID, PATH_TO_PICTURES);
        Directory right = directoryWasAdded(CLIENT_ID, PATH_TO_PICTURES);
        right.directoryWasRemoved();

        assertThat(left).isEqualTo(right);
        assertThat(left).hasSameHashCodeAs(right);
    }

}
