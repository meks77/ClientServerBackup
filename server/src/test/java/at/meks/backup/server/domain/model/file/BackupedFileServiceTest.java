package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.version.Content;
import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionId;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;
import lombok.SneakyThrows;
import org.assertj.core.api.RecursiveComparisonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackupedFileServiceTest {

    protected static final FileId FILE_ID = FileId.idFor(new ClientId("client1"), new PathOnClient(Path.of("whatever")));
    protected static final Content FILE_CONTENT = new Content(uri());

    @SneakyThrows
    private static URI uri() {
        return new URI("file://wherever");
    }

    @InjectMocks BackupedFileService service;

    @Mock BackupedFileRepository fileRepository;
    @Mock VersionRepository versionRespository;
    @Mock UtcClock clock;

    private ZonedDateTime currentTime;

    @BeforeEach
    void initClock() {
        currentTime = ZonedDateTime.now();
        lenient().when(clock.now()).thenReturn(currentTime);
    }

    private BackupedFile backupedFile() {
        return BackupedFile.newFileForBackup(FILE_ID);
    }

    @Nested
    class BackupTest {
        @Test void backupNewFile() {
            when(fileRepository.add(any())).thenAnswer(invocation -> invocation.getArgument(0));

            service.backup(FILE_ID, FILE_CONTENT);

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
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile()));

            service.backup(FILE_ID, FILE_CONTENT);

            verify(fileRepository, never()).add(any());
            assertCreatedVersion()
                    .isEqualTo(expectedVersion(backupedFile()));
        }

        @Test void latestVersionHashIsEqual() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(new FileHash(20));
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile));
            Content content = mock(Content.class);
            when(content.hash())
                    .thenReturn(new FileHash(20));


            service.backup(FILE_ID, content);

            verify(versionRespository, never()).add(any());
        }
    }

    @Nested
    class BackupIsNecessaryTest {

        @Test void fileDoesntExist() {
            FileHash fileHash = new FileHash(10);
            boolean result = service.isBackupNecessarry(FILE_ID, fileHash);
            assertThat(result).isTrue();
        }

        @Test void noVersionExists() {
            FileHash fileHash = new FileHash(10);
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile()));

            boolean result = service.isBackupNecessarry(FILE_ID, fileHash);

            assertThat(result).isTrue();
        }

        @Test void latestVersionHashIsDifferent() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(new FileHash(20));
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile));

            boolean result = service.isBackupNecessarry(FILE_ID, new FileHash(21));

            assertThat(result).isTrue();
        }

        @Test void latestVersionHashIsEqual() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(new FileHash(20));
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile));

            boolean result = service.isBackupNecessarry(FILE_ID, new FileHash(20));

            assertThat(result).isFalse();
        }

    }

}
