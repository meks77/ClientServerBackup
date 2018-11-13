package at.meks.backupclientserver.client;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

public class ErrorReporterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Logger logger;

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
    public void whenReportErrorWithMessageThenLoggerIsUsedAsExpected() {
        String message = "the expected message";
        errorReporter.reportError(message);
        Mockito.verify(logger).error(message);
    }
}
