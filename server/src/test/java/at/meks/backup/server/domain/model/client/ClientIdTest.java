package at.meks.backup.server.domain.model.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ClientIdTest {

    @Test
    void idTextIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ClientId(null))
                .withMessageContaining("id text");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void idTextIsBlank(String text) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ClientId(text))
                .withMessageContaining("id text");
    }

}