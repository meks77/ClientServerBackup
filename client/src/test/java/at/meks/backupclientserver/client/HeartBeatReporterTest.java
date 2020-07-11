package at.meks.backupclientserver.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeartBeatReporterTest {

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private ServerStatusService serverStatusService;

    @InjectMocks
    private HeartBeatReporter heartBeatReporter = new HeartBeatReporter();

    @Test
    void whenStartHearbeatThenUrlIsResolvedCorrect () {
        String expectedClientName = "exptectedUtHostN}ame";
        heartBeatReporter.reportHeartbeat();
    }

    @Test
    void whenStartHearbeatThenHeartbeatIsSentUsingDefinedIntervalSeconds() {
        // TODO verify remote service call
//        verify(jsonHttpClient, timeout(5000).times(3)).put(any(), any(), any(), eq(false));
    }

    @Test
    void whenStartHeartbeatReportingThenJsonHttpClientIsInvokedWithExpectedArgs() {
        heartBeatReporter.reportHeartbeat();
        // TODO verify remote service call
//        verify(jsonHttpClient, timeout(1000)).put(expectedUrl, null, Void.TYPE, false);
    }

    @Test
    @Disabled("the remote service call must be implemented")
    void whenExceptionIsThrownThenExceptionIsLogged() {
        IllegalArgumentException expectedException = new IllegalArgumentException();
        // TODO mock remote service call
        //        when(jsonHttpClient.put(any(), any(), any(), eq(false))).thenThrow(expectedException);

        heartBeatReporter.reportHeartbeat();

        verify(errorReporter, timeout(1500).times(1)).reportError(any(), same(expectedException));
    }

    @Test
    void whenHearbeatIsReportedSuccessfullyThenServerStatusIsSetToAvailable() {
        heartBeatReporter.reportHeartbeat();
        verify(serverStatusService, timeout(1000)).setServerAvailable(true);
    }

}