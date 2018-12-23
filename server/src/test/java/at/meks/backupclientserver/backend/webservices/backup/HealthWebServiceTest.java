package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.domain.ErrorLog;
import at.meks.backupclientserver.backend.services.ClientService;
import at.meks.backupclientserver.backend.services.ErrorReportService;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HealthWebServiceTest extends AbstractWebServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ClientService clientService;

    @Mock
    private ErrorReportService errorReportService;

    @InjectMocks
    private HealthWebService service;

    @Test
    public void givenHostNameWhenHeartbeatThenClientServiceIsInvoked() {
        String hostName = "theUtHostName";
        service.heartbeat(hostName);
        verify(clientService).updateHeartbeat(hostName);
    }

    @Test
    public void givenNullHostNameWhenHeartbeatThenClientServiceIsInvoked() {
        service.heartbeat(null);
        verify(clientService).updateHeartbeat(null);
    }

    @Test
    public void givenListSizeNullWhenGetErrorsThenErrorReportServiceIsInvokeWithListSize20() {
        service.getErrors(null);
        verify(errorReportService).getErrors(20);
    }

    @Test
    public void whenGetErrorsThenReturnsListOfErrorReportService() {
        List<ErrorLog> expectedList = new ArrayList<>();
        when(errorReportService.getErrors(any(Integer.class))).thenReturn(expectedList);

        List<ErrorLog> errors = service.getErrors(5);

        assertThat(errors).isSameAs(expectedList);
    }

    @Test
    public void whenHeartbeatThenExceptionHandlerIsInvoked() {
        verifyExceptionHandlerIsInvokedAndNothingElse(() -> service.heartbeat(null), clientService);
    }

    @Test
    public void whenGetErrorsThenExceptionHandlerIsInvoked() {
        verifyExceptionHandlerIsInvokedAndNothingElse(() -> service.getErrors(null), clientService);
    }
}
