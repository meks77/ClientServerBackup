package at.meks.backup.client.infrastructure.events;

import at.meks.backup.client.model.Events;
import at.meks.backup.client.model.FileEventListener;
import at.meks.backup.client.model.ScanDirectoryCommandListener;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Duration;

import static org.awaitility.Awaitility.waitAtMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@QuarkusTest
class QuarkusEventBusTest {

    @Inject
    QuarkusEventBus quarkusEventBus;

    @BeforeEach
    void removeAllListeners() {
        quarkusEventBus.deregisterAll();
    }

    @Nested
    class FileChanged {

        final Events.FileChangedEvent expectedEvent = new Events.FileChangedEvent(Path.of("myFile"));
        FileEventListener listener1 = Mockito.mock(FileEventListener.class);
        FileEventListener listener2 = Mockito.mock(FileEventListener.class);

        @Test
        void oneListenerIsRegistered() {
            quarkusEventBus.register(listener1);
            quarkusEventBus.fireFileChanged(expectedEvent);
            verifyListenerInvocation(listener1);
        }

        private void verifyListenerInvocation(FileEventListener listener) {
            waitAtMost(Duration.ofSeconds(1))
                    .pollInterval(Duration.ofMillis(50))
                    .untilAsserted(() -> verify(listener).onFileChanged(expectedEvent));
            verifyNoMoreInteractions(listener);
        }

        @Test
        void moreListenersAreRegistered() {
            quarkusEventBus.register(listener1);
            quarkusEventBus.register(listener2);

            quarkusEventBus.fireFileChanged(expectedEvent);

            verifyListenerInvocation(listener1);
            verifyListenerInvocation(listener2);
        }

    }

    @Nested
    class FireScanDirectories {

        final ScanDirectoryCommandListener listener1 = mock(ScanDirectoryCommandListener.class);
        final ScanDirectoryCommandListener listener2 = mock(ScanDirectoryCommandListener.class);

        @Test
        void oneListenerIsRegistered() {
            quarkusEventBus.register(listener1);
            quarkusEventBus.fireScanDirectories();
            verifyListenerInvocation(listener1);
        }

        private void verifyListenerInvocation(ScanDirectoryCommandListener listener) {
            waitAtMost(Duration.ofSeconds(1))
                    .pollInterval(Duration.ofMillis(50))
                    .untilAsserted(() -> verify(listener).scanDirectories());
        }

        @Test
        void moreListenersAreRegistered() {
            quarkusEventBus.register(listener1);
            quarkusEventBus.register(listener2);

            quarkusEventBus.fireScanDirectories();

            verifyListenerInvocation(listener1);
            verifyListenerInvocation(listener2);
        }
    }

}