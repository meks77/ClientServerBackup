package at.meks.backup.server.domain.model.file;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;

public class TestUtils {

    public interface Invoker {

        void invoke() throws Exception;

    }

    public static void wrapException(Invoker invoker) {
        try {
            invoker.invoke();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T wrapException(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Path pathOf(String path) {
        return wrapException(() -> Path.of(requireNonNull(TestUtils.class.getResource(path)).toURI()).toAbsolutePath());
    }
}
