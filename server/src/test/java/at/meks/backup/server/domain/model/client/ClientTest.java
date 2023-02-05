package at.meks.backup.server.domain.model.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ClientTest {

    @Test void nameIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Client.newClient(null));
    }

}