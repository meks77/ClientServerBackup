package at.meks.backup.server.domain.model.file;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class BackupTimeTest {

    @Test
    void timeIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new BackupTime(null));
    }

    @Test
    void timeIsNotNull() {
        assertThatNoException()
                .isThrownBy(() -> new BackupTime(ZonedDateTime.now()));
    }

}