package at.meks.backup.client.usecases;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class FileChangeQueueTest {

    FileChangeQueue fileChangeQueue = new FileChangeQueue();
    private long duration;

    @Test
    @Timeout(1)
    void changeIsPublishedAfter50Millis() {
        Path path = Path.of("whatever");
        Path result = trackTime(() -> {
            fileChangeQueue.add(path);
            return fileChangeQueue.next();
        });
        assertThat(duration)
                .isGreaterThanOrEqualTo(50);
        assertThat(result)
                .isEqualTo(path);
        assertThat(fileChangeQueue.isEmpty())
                .isTrue();
    }

    private <T> T trackTime(Supplier<T> method) {
        long start = System.currentTimeMillis();
        T result = method.get();
        long end = System.currentTimeMillis();
        duration = end - start;
        return result;
    }

    @Test
    @Timeout(1)
    void changeIsPublished50MillisAfterLastUpdate() {
        Path path = Path.of("whatever");
        fileChangeQueue.add(path);
        wait20Millis();
        fileChangeQueue.add(path);
        wait20Millis();
        Path result = trackTime(() -> {
            fileChangeQueue.add(path);
            return fileChangeQueue.next();
        });
        assertThat(duration)
                .isGreaterThanOrEqualTo(50);
        assertThat(result)
                .isEqualTo(path);
        assertThat(fileChangeQueue.isEmpty())
                .isTrue();
    }

    private static void wait20Millis() {
        Awaitility.await().pollDelay(20, TimeUnit.MILLISECONDS).untilTrue(new AtomicBoolean(true));
    }
}
