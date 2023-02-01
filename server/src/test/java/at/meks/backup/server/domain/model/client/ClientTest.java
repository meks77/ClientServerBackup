package at.meks.backup.server.domain.model.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ClientTest {

    @Test void idIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Client(null, new ClientName("whatever")));
    }

    @Test void nameIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Client(new ClientId("whatever"), null));
    }

}