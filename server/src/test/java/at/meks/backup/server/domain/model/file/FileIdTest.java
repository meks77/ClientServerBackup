package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class FileIdTest {

    @Test void clientIdIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> FileId.idFor(null, new PathOnClient(Path.of("whatever"))));
    }

    @Test void pathIdNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> FileId.idFor(ClientId.newId(), null));
    }

}