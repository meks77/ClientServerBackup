package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.ClientService;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class HealthWebServiceTest extends AbstractWebServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ClientService clientService;

    @InjectMocks
    private HealthWebService service;

    @Test
    public void givenHostNameWhenHeartbeatThenClientServiceIsInvoked() {
        String hostName = "theUtHostName";
        service.heartbeat(hostName);
        Mockito.verify(clientService).updateHeartbeat(hostName);
    }

    @Test
    public void givenNullHostNameWhenHeartbeatThenClientServiceIsInvoked() {
        service.heartbeat(null);
        Mockito.verify(clientService).updateHeartbeat(null);
    }

    @Test
    public void whenHeartbeatThenExceptionHandlerIsInoked() {
        verifyExceptionHandlerIsInvokedAndNothingElse(() -> service.heartbeat(null), clientService);
    }
}
