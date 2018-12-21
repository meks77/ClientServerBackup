package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.http.HttpUrlResolver;
import at.meks.backupclientserver.client.http.JsonHttpClient;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HeartBeatReporterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Logger logger;

    @Mock
    private HttpUrlResolver urlResolver;

    @Mock
    private JsonHttpClient jsonHttpClient;

    @Mock
    private SystemService systemService;

    @Mock
    private ErrorReporter errorReporter;

    @InjectMocks
    private HeartBeatReporter heartBeatReporter = new HeartBeatReporter();

    @Test
    public void whenStartHearbeatThenUrlIsResolvedCorrect() {
        String expectedClientName = "exptectedUtHostName";
        when(systemService.getHostname()).thenReturn(expectedClientName);
        heartBeatReporter.startHeartbeatReporting();
        verify(urlResolver, timeout(1000)).getWebserviceUrl("health", "heartbeat/" + expectedClientName);
    }

    @Test
    public void whenStartHearbeatThenHeartbeatIsSentUsingDefinedIntervalSeconds() throws IllegalAccessException {
        FieldUtils.writeField(heartBeatReporter, "interval", 2, true);

        heartBeatReporter.startHeartbeatReporting();

        verify(jsonHttpClient, timeout(5000).times(3)).put(any(), any(), any());
    }

    @Test
    public void whenStartHeartbeatReportingThenJsonHttpClientIsInvokedWithExpectedArgs() {
        String expectedUrl = "expected url for the hearbeat";
        when(urlResolver.getWebserviceUrl(any(), any())).thenReturn(expectedUrl);

        heartBeatReporter.startHeartbeatReporting();

        verify(jsonHttpClient, timeout(1000)).put(expectedUrl, null, Void.TYPE);
    }

    @Test
    public void whenExceptionIsThrownThenExceptionIsLogged() {
        IllegalArgumentException expectedException = new IllegalArgumentException();
        when(jsonHttpClient.put(any(), any(), any())).thenThrow(expectedException);

        heartBeatReporter.startHeartbeatReporting();

        verify(errorReporter, timeout(1500).times(1)).reportError(any(), same(expectedException));
    }
}