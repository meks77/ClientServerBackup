package at.meks.backupclientserver.context.backup;

import at.meks.backupclientserver.api.BackupFile;
import at.meks.backupclientserver.api.ClientId;
import at.meks.backupclientserver.api.FileBackedUp;
import at.meks.backupclientserver.api.FileProperties;
import at.meks.backupclientserver.api.ManagedPath;
import at.meks.backupclientserver.api.ManagedRootDir;
import lombok.SneakyThrows;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

class ManagedFileTest {

    private FixtureConfiguration<ManagedFile> fixture = new AggregateTestFixture<>(ManagedFile.class);

    @SneakyThrows
    @Test
    void testBackupOfNewUploadedFile(@TempDir Path tempDir) {
        Path uploadedFile = Files.createFile(tempDir.resolve("uploadedFile.txt"));
        final BackupFile backupCmd = new BackupFile(
                new FileProperties(
                        new ManagedPath(new ManagedRootDir("dirPath"), "a/b/c", new ClientId("utClient")),
                        uploadedFile.getFileName().toString()),
                uploadedFile.toString());
        fixture.given()
                .when(backupCmd)
                .expectEvents(new FileBackedUp(backupCmd.getFileProperties().getId(), uploadedFile));
    }

}