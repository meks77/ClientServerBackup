package at.meks.backupclientserver.client;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@QuarkusTest
public class HeartBeatReporterTest {

    @InjectMock
    ErrorReporter errorReporter;

    @InjectMock
    ServerStatusService serverStatusService;

    @Inject
    HeartBeatReporter heartBeatReporter = new HeartBeatReporter();

    @Test
    public void whenStartHearbeatThenUrlIsResolvedCorrect () {
        String expectedClientName = "exptectedUtHostN}ame";
        heartBeatReporter.reportHeartbeat();
    }

    @Test
    public void whenStartHearbeatThenHeartbeatIsSentUsingDefinedIntervalSeconds() {
        // TODO verify remote service call
//        verify(jsonHttpClient, timeout(5000).times(3)).put(any(), any(), any(), eq(false));
    }

    @Test
    public void whenStartHeartbeatReportingThenJsonHttpClientIsInvokedWithExpectedArgs() {
        heartBeatReporter.reportHeartbeat();
        // TODO verify remote service call
//        verify(jsonHttpClient, timeout(1000)).put(expectedUrl, null, Void.TYPE, false);
    }

    @Test
    @Disabled("the remote service call must be implemented")
    public void whenExceptionIsThrownThenExceptionIsLogged() {
        IllegalArgumentException expectedException = new IllegalArgumentException();
        // TODO mock remote service call
        //        when(jsonHttpClient.put(any(), any(), any(), eq(false))).thenThrow(expectedException);

        heartBeatReporter.reportHeartbeat();

        verify(errorReporter, timeout(1500).times(1)).reportError(any(), same(expectedException));
    }

    @Test
    public void whenHearbeatIsReportedSuccessfullyThenServerStatusIsSetToAvailable() {
        heartBeatReporter.reportHeartbeat();
        verify(serverStatusService, timeout(1000)).setServerAvailable(true);
    }


}