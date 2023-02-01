package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.time.UtcClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BackupedFileServiceTest {

    protected static final BusinessKey BUSINESS_KEY = BusinessKey.idFor(new ClientId("client1"), new PathOnClient(Path.of("whatever")));
    protected static final Content FILE_CONTENT = new Content(new ByteArrayInputStream(new byte[5]));
    @InjectMocks BackupedFileService service;

    @Mock BackupedFileRepository fileRepository;
    @Mock VersionRepository versionRespository;
    @Mock
    UtcClock clock;
    private ZonedDateTime currentTime;

    @BeforeEach
    void initClock() {
        currentTime = ZonedDateTime.now();
        lenient().when(clock.now()).thenReturn(currentTime);
    }

    @Test void backupNewFile() {
        BackupedFile backupedFile = BackupedFile.newFileForBackup(BUSINESS_KEY);
        when(fileRepository.add(backupedFile))
                .thenReturn(backupedFile);

        service.backup(BUSINESS_KEY, FILE_CONTENT);

        verify(fileRepository)
                .add(backupedFile);
        assertThat(createdVersion())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedVersion(backupedFile));
    }

    private Version createdVersion() {
        ArgumentCaptor<Version> versionCaptor = ArgumentCaptor.forClass(Version.class);
        verify(versionRespository)
                .add(versionCaptor.capture());
        return versionCaptor.getValue();
    }

    private Version expectedVersion(BackupedFile backupedFile) {
        return new Version(
                new VersionId(UUID.randomUUID()),
                backupedFile.id(),
                new BackupTime(currentTime),
                FILE_CONTENT);
    }

}
