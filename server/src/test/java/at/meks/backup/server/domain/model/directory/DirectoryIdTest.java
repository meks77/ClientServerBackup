package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static at.meks.backup.server.domain.model.client.ClientId.existingId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DirectoryIdTest {

    protected static final PathOnClient PATH = new PathOnClient(Path.of("aDirectory"));

    @Test
    void clientIdIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DirectoryId.idFor(null, PATH))
                .withMessageContaining("clientId");
    }

    @Test
    void pathIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DirectoryId.idFor(ClientId.newId(), null))
                .withMessageContaining("path");
    }

    @Test
    void pathIsLinuxPath() {
        String clientId = "clientIdY";
        String path = "/home/theusername/Pictures";

        DirectoryId result = DirectoryId.idFor(
                existingId(clientId),
                new PathOnClient(Paths.get(path)));

        assertThat(result.text())
                .isEqualTo("%s:%s", clientId, path);
    }

    @Test
    void pathIsWindowsPath() {
        String clientId = "clientIdX";
        String path = "C:\\Users\\theusername\\Documents";

        DirectoryId result = DirectoryId.idFor(
                existingId(clientId),
                new PathOnClient(Paths.get(path)));

        assertThat(result.text())
                .isEqualTo("%s:%s", clientId, path);
    }

}