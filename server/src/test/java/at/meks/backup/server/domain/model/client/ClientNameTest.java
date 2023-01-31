package at.meks.backup.server.domain.model.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ClientNameTest {

    @ParameterizedTest
    @ValueSource(strings = { "", " "})
    void nameIsBlank(String text) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ClientName(text))
                .withMessageContaining("client name text");
    }

    @Test
    void nameIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ClientName(null))
                .withMessageContaining("client name text");
    }

}