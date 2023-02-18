package at.meks.backup.client.infrastructure.events;

import at.meks.backup.client.model.Events;
import at.meks.backup.client.model.FileEventListener;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.nio.file.Path;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

//TODO:it seems the events are async. Therefore the verification must consider that
@QuarkusTest
class QuarkusEventBusTest {

    FileEventListener listener1 = Mockito.mock(FileEventListener.class);
    FileEventListener listener2 = Mockito.mock(FileEventListener.class);

    @Inject
    QuarkusEventBus quarkusEventBus;

    @BeforeEach
    void removeAllListeners() {
        quarkusEventBus.deregisterAll();
    }

    @Nested
    class FileChanged {

        private final Events.FileChangedEvent expectedEvent = new Events.FileChangedEvent(Path.of("myFile"));

        @Test
        void oneListenerIsRegistered() {
            quarkusEventBus.register(listener1);
            quarkusEventBus.fireFileChanged(expectedEvent);
            verifyListenerInvocation(listener1);
        }

        private void verifyListenerInvocation(FileEventListener listener) {
            verify(listener).onFileChanged(expectedEvent);
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
    class FileNeedsBackup {

        private final Events.FileNeedsBackupEvent expectedEvent = new Events.FileNeedsBackupEvent(Path.of("myFile"));

        @Test
        void oneListenerIsRegistered() {
            quarkusEventBus.register(listener1);
            quarkusEventBus.fireFileNeedsBackup(expectedEvent);
            verifyListenerInvocation(listener1);
        }

        private void verifyListenerInvocation(FileEventListener listener) {
            verify(listener).onFileNeedsBackup(expectedEvent);
            verifyNoMoreInteractions(listener);
        }

        @Test
        void moreListenersAreRegistered() {
            quarkusEventBus.register(listener1);
            quarkusEventBus.register(listener2);

            quarkusEventBus.fireFileNeedsBackup(expectedEvent);

            verifyListenerInvocation(listener1);
            verifyListenerInvocation(listener2);
        }

    }

}