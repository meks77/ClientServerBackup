package at.meks.backupclientserver.client.http;

import at.meks.backupclientserver.client.ServerStatusService;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class JsonHttpClientTest {

    private static final String URL = "expectedUrl";
    private static final String INPUT = "expectedInput";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ServerStatusService serverStatusService;

    @Mock
    private CloseableHttpClient httpClient;

    @InjectMocks
    private JsonHttpClient jsonHttpClient;

    @Test
    public void givenWaitForServerWhenWhenPutThenHttpIsInvokedWhenServerIsAvailable() {
        jsonHttpClient.put(URL, INPUT, Void.TYPE);
        verifyHttpIsInvokedWhenServerIsAvailable();
    }

    private void verifyHttpIsInvokedWhenServerIsAvailable() {
        verify(serverStatusService).runWhenServerIsAvailable(any());
        verifyZeroInteractions(httpClient);
    }

    @Test
    public void givenNoWaitForServerWhenPutThenHttpIsInvoedWhenServerIsAvailable() throws IOException {
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);

        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        jsonHttpClient.put(URL, INPUT, Void.TYPE, false);
        verifyZeroInteractions(serverStatusService);
    }

    @Test
    public void whenPostThenHttpIsInvokedWhenServerIsAvailable() {
        jsonHttpClient.post(URL, INPUT, Void.TYPE);
        verifyHttpIsInvokedWhenServerIsAvailable();
    }

    @Test
    public void whenDeleteThenHttpIsInvokedWhenServerIsAvailable() {
        jsonHttpClient.delete(URL, INPUT);
        verifyHttpIsInvokedWhenServerIsAvailable();
    }

}