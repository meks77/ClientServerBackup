package at.meks.backupclientserver.client;

import org.junit.Test;

import java.security.InvalidKeyException;

import static org.fest.assertions.api.Assertions.assertThat;

public class ClientBackupExceptionTest {

    @Test
    public void givenMessageOnlyWhenGetMessageReturnsExpected() {
        String expectedMsg = "expectedMsg";
        assertThat(new ClientBackupException(expectedMsg).getMessage()).isEqualTo(expectedMsg);
    }

    @Test
    public void givenMessageAndCauseWhenGetMessageReturnsExpected() {
        String expectedMsg = "expectedMsg";
        assertThat(new ClientBackupException(expectedMsg, new InvalidKeyException()).getMessage()).isEqualTo(expectedMsg);
    }

    @Test
    public void givenMessageAndCauseWhenGetCauseReturnsExpected() {
        InvalidKeyException cause = new InvalidKeyException();
        assertThat(new ClientBackupException("expectedMsg", cause).getCause()).isSameAs(cause);
    }
}
