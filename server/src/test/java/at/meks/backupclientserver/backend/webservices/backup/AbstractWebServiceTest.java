package at.meks.backupclientserver.backend.webservices.backup;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.mockito.Mock;

import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AbstractWebServiceTest {

    @Mock
    ExceptionHandler exceptionHandler;

    @Before
    public void mockExceptionHandler() {
        doAnswer(invocation -> {
                    ((Runnable)invocation.getArgument(1)).run();
                    return Void.TYPE;
                }).when(exceptionHandler).runReportingException(any(), any(Runnable.class));
        doAnswer(invocation -> ((Callable<?>) invocation.getArgument(1)).call())
                .when(exceptionHandler).runReportingException(any(), any(Callable.class));
    }

    void verifyExceptionHandlerIsInvokedAndNothingElse(Runnable serviceMethod, Object...otherMocks) {
        reset(exceptionHandler);

        serviceMethod.run();

        verify(exceptionHandler).runReportingException(any(), any(Runnable.class));
        verifyNoMoreInteractions(ArrayUtils.add(otherMocks, exceptionHandler));
    }

    void verifyExceptionHandlerIsInvokedAndNothingElse(Callable<?> serviceMethod, Object...otherMocks) {
        verifyExceptionHandlerIsInvokedAndNothingElse(null, serviceMethod, otherMocks);
    }

    void verifyExceptionHandlerIsInvokedAndNothingElse(Object returnValue, Callable<?> serviceMethod, Object...otherMocks) {
        reset(exceptionHandler);
        if (returnValue != null) {
            when(exceptionHandler.runReportingException(any(), any(Callable.class))).thenReturn(returnValue);
        }
        try {
            serviceMethod.call();
        } catch (Exception e) {
            throw new IllegalStateException("there shouldn't be an exception thrown", e);
        }

        verify(exceptionHandler).runReportingException(any(), any(Callable.class));
        verifyNoMoreInteractions(ArrayUtils.add(otherMocks, exceptionHandler));
    }
}
