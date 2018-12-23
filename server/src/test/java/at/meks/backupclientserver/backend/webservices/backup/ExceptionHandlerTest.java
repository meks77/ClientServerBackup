package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.ErrorReportService;
import at.meks.backupclientserver.backend.services.ServerBackupException;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExceptionHandlerTest {

    public static final String EXCEPTION_MESSAGE = "whatever";
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ErrorReportService errorReportService;

    @Mock
    private Supplier<String> execInfoSupplier;

    @Mock
    private Callable callable;

    @Mock
    private Runnable runnable;

    @InjectMocks
    private ExceptionHandler handler = new ExceptionHandler();

    @Test
    public void givenCallableWhenRunReportingExceptionThenCallableIsInvoked() throws Exception {
        handler.runReportingException(execInfoSupplier, callable);
        verify(callable).call();
    }

    @Test
    public void givenRunnableWhenRunReportingExceptionThenRunnableIsInvoked() {
        handler.runReportingException(execInfoSupplier, runnable);
        verify(runnable).run();
    }

    @Test
    public void givenCallableWhenRunReportingExceptionThenReturnsValueOfCallable() throws Exception {
        String extectedValue = "extectedValue";
        when(callable.call()).thenReturn(extectedValue);

        Object result = handler.runReportingException(execInfoSupplier, callable);

        assertThat(result).isEqualToComparingFieldByField(extectedValue);
    }

    @Test
    public void givenCallableWhenExceptionIsThrownThenExceptionIsThrownWithCause() throws Exception {
        IllegalArgumentException excpectedCause = new IllegalArgumentException(EXCEPTION_MESSAGE);
        when(callable.call()).thenThrow(excpectedCause);
        expectedException.expect(ServerBackupException.class);
        expectedException.expectCause(CoreMatchers.is(excpectedCause));

        handler.runReportingException(execInfoSupplier, callable);
    }

    @Test
    public void givenRunnableWhenExceptionIsThrownThenExceptionIsThrownWithCause() {
        IllegalArgumentException excpectedCause = new IllegalArgumentException(EXCEPTION_MESSAGE);
        doThrow(excpectedCause).when(runnable).run();
        expectedException.expect(ServerBackupException.class);
        expectedException.expectCause(CoreMatchers.is(excpectedCause));

        handler.runReportingException(execInfoSupplier, runnable);
    }

    @Test
    public void givenCallableWhenExceptionIsThrownThenExceptionIsReported() throws Exception {
        IllegalArgumentException excpectedCause = new IllegalArgumentException(EXCEPTION_MESSAGE);
        when(callable.call()).thenThrow(excpectedCause);
        try {
            handler.runReportingException(execInfoSupplier, callable);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (Exception ignore) {
        }
        verify(errorReportService).addError(any(), any(), eq(excpectedCause));
    }

    @Test
    public void givenRunnableWhenExceptionIsThrownThenExceptionIsReported() {
        IllegalArgumentException excpectedCause = new IllegalArgumentException(EXCEPTION_MESSAGE);
        doThrow(excpectedCause).when(runnable).run();
        try {
            handler.runReportingException(execInfoSupplier, runnable);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (Exception ignore) {
        }
        verify(errorReportService).addError(any(), any(), eq(excpectedCause));
    }

    @Test
    public void givenCallableWhenExceptionIsThrownThenInvocationInfoIsReported() throws Exception {
        String expectedExecInfo = "expectedExecInfo";
        when(execInfoSupplier.get()).thenReturn(expectedExecInfo);
        when(callable.call()).thenThrow(new IllegalArgumentException());
        try {
            handler.runReportingException(execInfoSupplier, callable);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (Exception ignore) {
        }
        verify(errorReportService).addError(any(), eq(expectedExecInfo), any());
    }

    @Test
    public void givenRunnableWhenExceptionIsThrownThenInvocationInfoIsReported() {
        String expectedExecInfo = "expectedExecInfo";
        when(execInfoSupplier.get()).thenReturn(expectedExecInfo);
        doThrow(new IllegalArgumentException()).when(runnable).run();
        try {
            handler.runReportingException(execInfoSupplier, runnable);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (Exception ignore) {
        }
        verify(errorReportService).addError(any(), eq(expectedExecInfo), any());
    }

    @Test
    public void givenCallableWhenExceptionIsThrownThenHostNameIsReported() throws Exception {
        when(callable.call()).thenThrow(new IllegalArgumentException());
        try {
            handler.runReportingException(execInfoSupplier, callable);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (Exception ignore) {
        }
        String expectedHost = InetAddress.getLocalHost().getHostName();
        verify(errorReportService).addError(eq(expectedHost), any(), any());
    }

    @Test
    public void givenRunnableWhenExceptionIsThrownThenHostNameIsReported() throws Exception {
        doThrow(new IllegalArgumentException()).when(runnable).run();
        try {
            handler.runReportingException(execInfoSupplier, runnable);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (Exception ignore) {
        }
        String expectedHost = InetAddress.getLocalHost().getHostName();
        verify(errorReportService).addError(eq(expectedHost), any(), any());
    }

}