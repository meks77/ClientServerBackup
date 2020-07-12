package at.meks.backupclientserver.client.backup.model;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.vertx.core.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.newInputStream;
import static java.util.Optional.of;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class BackupCandidateServiceTest {

    @InjectMock
    BackupCandidateRepository repository;

    @Inject
    EventBus eventBus;

    @InjectSpy
    BackupCandidateService backupCandidateService;

    @Test
    void whenFileIsCreatedFileIsBackuped(@TempDir Path tempDir) throws IOException {
        final Client client = new Client("clientId");
        final Path file = Files.createFile(tempDir.resolve("testFile.txt"));
        final EventType eventType = EventType.CREATED;
        final FileChangedEvent event = new FileChangedEvent(client, file, eventType);
        eventBus.publish("backup", event);
        verify(repository, Mockito.timeout(200).times(0)).save(any());
        verify(repository, Mockito.timeout(700)).getLatestMd5Hex(client, file);
        verify(repository, Mockito.timeout(1000).times(1)).save(new BackupCandidate(client, file, eventType, null));
        verify(backupCandidateService, times(2)).fileRegisteredForBackup(event);
    }

    @Test
    void whenFileIsModifiedFileIsBackuped(@TempDir Path tempDir) throws IOException {
        final Client client = new Client("clientId");
        final Path file = Files.createFile(tempDir.resolve("testFile.txt"));
        final EventType eventType = EventType.MODIFIED;
        final FileChangedEvent event = new FileChangedEvent(client, file, eventType);
        eventBus.publish("backup", event);
        verify(repository, Mockito.timeout(200).times(0)).save(any());
        verify(repository, Mockito.timeout(700)).getLatestMd5Hex(client, file);
        verify(repository, Mockito.timeout(1000).times(1)).save(new BackupCandidate(client, file, eventType, null));
        verify(backupCandidateService, times(2)).fileRegisteredForBackup(event);
    }

    @Test
    void whenFileIsModifiedButStillEqualsFileIsNotBackuped(@TempDir Path tempDir) throws IOException {
        final Client client = new Client("clientId");
        final Path file = Files.createFile(tempDir.resolve("testFile.txt"));
        final EventType eventType = EventType.MODIFIED;
        final FileChangedEvent event = new FileChangedEvent(client, file, eventType);

        when(repository.getLatestMd5Hex(client, file)).thenReturn(of(md5Hex(newInputStream(file))));

        eventBus.publish("backup", event);
        verify(repository, Mockito.timeout(700)).getLatestMd5Hex(client, file);
        verify(repository, times(0)).save(new BackupCandidate(client, file, eventType, null));
        verify(backupCandidateService, times(2)).fileRegisteredForBackup(event);
    }


}