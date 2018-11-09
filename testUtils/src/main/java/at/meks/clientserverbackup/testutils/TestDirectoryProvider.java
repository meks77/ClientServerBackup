package at.meks.clientserverbackup.testutils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestDirectoryProvider {

    private TestDirectoryProvider() {

    }

    public static Path createTempDirectory() {
        try {
            return Files.createTempDirectory(Paths.get(TestDirectoryProvider.class.getResource("/").toURI()), "utTmpDir");
        } catch (Exception e) {
            throw new AssertionError("temp dir couldn't be created");
        }
    }
}
