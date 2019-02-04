package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.http.HttpUrlResolver;
import at.meks.backupclientserver.client.http.JsonHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import java.net.ConnectException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ErrorReporterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Logger logger;

    @Mock
    private SystemService systemService;

    @Mock
    private JsonHttpClient jsonHttpClient;

    @Mock
    private HttpUrlResolver httpUrlResolver;

    @InjectMocks
    private ErrorReporter errorReporter;

    @Test
    public void whenReportErrorWithExceptionThenLoggerIsUsedAsExpected() {
        RuntimeException expectedException = new RuntimeException("ut test exception");
        String message = "the expected message";
        errorReporter.reportError(message, expectedException);
        Mockito.verify(logger).error(message, expectedException);
    }

    @Test
    public void givenServerNotAvailableWhenReportErrorThenReportIsNotSentToServer() {
        RuntimeException expectedException = new RuntimeException("ut test exception", new ConnectException());
        errorReporter.reportError("whatever", expectedException);

        verifyZeroInteractions(jsonHttpClient);
    }

    @Test
    public void givenServerAvailableWhenReportErrorThenReportIsSentToServer() {
        String hostName = "theHostName";
        when(systemService.getHostname()).thenReturn(hostName);
        String url = "theExpectedUrl";
        when(httpUrlResolver.getWebserviceUrl("health", "error/" + hostName)).thenReturn(url);

        RuntimeException expectedException = new RuntimeException("ut test exception");
        errorReporter.reportError("whatever", expectedException);

        verify(jsonHttpClient).put(eq(url), any(), eq(Void.TYPE));
    }

}
