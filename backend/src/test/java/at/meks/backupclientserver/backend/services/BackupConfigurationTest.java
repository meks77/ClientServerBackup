package at.meks.backupclientserver.backend.services;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class BackupConfigurationTest {

    private BackupConfiguration config = new BackupConfiguration();

    @Test
    public void givenNotExistingDirWhenGetApplicationRootDirectoryThenTheDirectoryCreated() throws IOException,
            IllegalAccessException {
        Path tempDir = Files.createTempDirectory("utRootDir");
        Path expectedRootDir = Paths.get(tempDir.toString(), "expectedRootDir");
        FieldUtils.writeField(config, "applicationRoot", expectedRootDir.toString(), true);

        Path result = config.getApplicationRootDirectory();
        Assert.assertTrue(result.toFile().exists());
    }

    @Test
    public void givenDirWhenGetApplicationRootDirectoryThenPathOfConfigIsReturned() throws IOException,
            IllegalAccessException {
        Path expectedRootDir = Files.createTempDirectory("utRootDir");
        FieldUtils.writeField(config, "applicationRoot", expectedRootDir.toString(), true);

        Path result = config.getApplicationRootDirectory();
        Assert.assertEquals(Paths.get(expectedRootDir.toString(), ".clientServerBackup", "backups"), result);
    }

    @Test(expected = ServerBackupException.class)
    public void givenNoDirWhenGetApplicationRootDirectoryThenExceptionIsThrown() {
        config.getApplicationRootDirectory();
    }
}
