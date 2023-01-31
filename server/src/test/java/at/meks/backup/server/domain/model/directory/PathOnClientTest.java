package at.meks.backup.server.domain.model.directory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PathOnClientTest {

    @Test
    void pathIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PathOnClient(null))
                .withMessageContaining("path");
    }

}