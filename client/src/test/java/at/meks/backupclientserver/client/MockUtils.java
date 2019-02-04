package at.meks.backupclientserver.client;

import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MockUtils {

    public static void mockDelegate(ServerStatusService serverStatusService) {
        when(serverStatusService.runWhenServerIsAvailable(any()))
                .thenAnswer(invocation -> ((Callable<?>) invocation.getArgument(0)).call());
    }
}
