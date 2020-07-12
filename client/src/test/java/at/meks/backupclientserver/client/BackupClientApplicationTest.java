package at.meks.backupclientserver.client;

import at.meks.validation.result.ValidationException;
import io.quarkus.runtime.Quarkus;
import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith({MockitoExtension.class, JMockitExtension.class})
public class BackupClientApplicationTest {

    @Mock
    private ApplicationConfig applicationConfig;

    @InjectMocks
    private BackupClientApplication application;

    @BeforeEach
    void mockQuarkus() {
        new MockUp<Quarkus>() {
            @mockit.Mock
            public void waitForExit() {

            }
        };
    }

    @Test
    void givenInvalidConfig() throws Exception {
        doThrow(mock(ValidationException.class)).when(applicationConfig).validate();

        assertThrows(ValidationException.class,
                () -> application.run());
    }

    @Test
    void givenValidConfig() throws Exception {
        assertEquals(0, application.run());
    }
}