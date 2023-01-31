package at.meks.backup.server.domain.model.directory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DirectoryIdTest {

    @Test
    void idIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DirectoryId(null))
                .withMessageContaining("id text");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void idIsBlank(String idText) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DirectoryId(idText))
                .withMessageContaining("id text");
    }

}