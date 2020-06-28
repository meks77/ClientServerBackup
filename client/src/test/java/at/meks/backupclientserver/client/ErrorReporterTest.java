package at.meks.backupclientserver.client;

import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit5.JMockitExtension;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ConnectException;

@ExtendWith(MockitoExtension.class)
public class ErrorReporterTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private ErrorReporter errorReporter = new ErrorReporter();

    @Test
    public void givenServerNotAvailableWhenReportErrorThenReportIsNotSentToServer() {
        RuntimeException expectedException = new RuntimeException("ut test exception", new ConnectException());
        errorReporter.reportError("whatever", expectedException);
        // TODO verify zero remote invocations
//        verifyZeroInteractions(jsonHttpClient);
    }

    @Test
    @Disabled("reporting doesn't work currently")
    public void givenServerAvailableWhenReportErrorThenReportIsSentToServer() {
        String hostName = "theHostName";
        String url = "theExpectedUrl";

        RuntimeException expectedException = new RuntimeException("ut test exception");
        errorReporter.reportError("whatever", expectedException);

        //TODO verify remote service invocation
//        verify(jsonHttpClient).put(eq(url), any(), eq(Void.TYPE));
    }

}
