package at.meks.backupclientserver.client;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class ServerStatusServiceTest {

    private ServerStatusService serverStatusService = new ServerStatusService();

    @Test
    public void givenServerIsAvailableWhenRunThenCallableIsInvoked() throws Exception {
        serverStatusService.setServerAvailable(true);
        @SuppressWarnings("unchecked")
        Callable<Void> callable = mock(Callable.class);
        serverStatusService.runWhenServerIsAvailable(callable);

        verify(callable, timeout(100)).call();
    }

    @Test
    public void givenServerNotAvailableButGetsAvailableWhenRunThenCallableIsInvoked() throws Exception {
        serverStatusService.setServerAvailable(false);
        @SuppressWarnings("unchecked")
        Callable<Void> callable = mock(Callable.class);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.submit(() -> serverStatusService.runWhenServerIsAvailable(callable));

        verify(callable, timeout(700).times(0)).call();
        serverStatusService.setServerAvailable(true);
        verify(callable, timeout(500)).call();
    }

    @Test
    public void givenServerNotAvailableWhenRunThenCallableIsNotInvoked() throws Exception {
        serverStatusService.setServerAvailable(false);
        @SuppressWarnings("unchecked")
        Callable<Void> callable = mock(Callable.class);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.submit(() -> serverStatusService.runWhenServerIsAvailable(callable));
        verify(callable, after(1000).times(0)).call();
    }



}