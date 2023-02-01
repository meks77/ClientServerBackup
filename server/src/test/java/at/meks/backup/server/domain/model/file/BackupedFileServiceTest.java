package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.version.Content;
import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionId;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;
import org.assertj.core.api.RecursiveComparisonAssert;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackupedFileServiceTest {

    protected static final FileId BUSINESS_KEY = FileId.idFor(new ClientId("client1"), new PathOnClient(Path.of("whatever")));
    protected static final Content FILE_CONTENT = new Content(new ByteArrayInputStream(new byte[5]));
    @InjectMocks BackupedFileService service;

    @Mock BackupedFileRepository fileRepository;
    @Mock
    VersionRepository versionRespository;
    @Mock
    UtcClock clock;
    private ZonedDateTime currentTime;

    @BeforeEach
    void initClock() {
        currentTime = ZonedDateTime.now();
        lenient().when(clock.now()).thenReturn(currentTime);
    }

    @Test void backupNewFile() {
        service.backup(BUSINESS_KEY, FILE_CONTENT);

        verify(fileRepository)
                .add(backupedFile());
        assertCreatedVersion()
                .isEqualTo(expectedVersion(backupedFile()));
    }

    private RecursiveComparisonAssert<?> assertCreatedVersion() {
        return assertThat(createdVersion())
                .usingRecursiveComparison()
                .ignoringFields("id");
    }

    private static BackupedFile backupedFile() {
        return BackupedFile.newFileForBackup(BUSINESS_KEY);
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

    @Test void backupExistingFile() {
        when(fileRepository.get(BUSINESS_KEY))
                .thenReturn(Optional.of(backupedFile()));

        service.backup(BUSINESS_KEY, FILE_CONTENT);

        verify(fileRepository, never()).add(any());
        assertCreatedVersion()
                .isEqualTo(expectedVersion(backupedFile()));
    }

}
