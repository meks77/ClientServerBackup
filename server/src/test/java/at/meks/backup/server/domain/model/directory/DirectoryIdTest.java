package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

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

}