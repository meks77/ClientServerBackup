package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.ErrorLog;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorLogRepositoryTest {

    private ErrorLogRepository repository = new ErrorLogRepository();

    @Test
    public void verifyGetEntityClassReturnsErrorLog() {
        assertThat(repository.getEntityClass()).isEqualTo(ErrorLog.class);
    }

}