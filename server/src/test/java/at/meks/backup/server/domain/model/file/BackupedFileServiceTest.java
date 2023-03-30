package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.client.ClientService;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;
import at.meks.backup.shared.model.Checksum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Optional;

import static at.meks.backup.server.domain.model.file.TestUtils.wrapException;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BackupedFileServiceTest {

    private Path fileForBackup;

    protected FileId fileId;

    @InjectMocks BackupedFileService service;

    @Mock BackupedFileRepository fileRepository;
    @Mock VersionRepository versionRespository;
    @Mock UtcClock clock;
    @Mock ClientService clientService;

    private final ZonedDateTime currentTime = ZonedDateTime.now();

    @BeforeEach
    void initArgs() {
        wrapException(()  -> {
            lenient().when(clock.now()).thenReturn(currentTime);
            fileForBackup = Path.of(requireNonNull(getClass().getResource("/fileuploads/file1.txt")).toURI());
            fileId = FileId.idFor(ClientId.newId(), new PathOnClient(fileForBackup));
        });
    }

    private BackupedFile backupedFile() {
        return BackupedFile.newFileForBackup(fileId);
    }

    @Nested
    class BackupTest {

        @Test void backupNewFile() {
            when(fileRepository.add(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            service.backup(fileId, fileForBackup);

            verify(fileRepository)
                    .add(backupedFile());

            verifyVersion(fileForBackup);
        }

        private void verifyVersion(Path fileForBackup) {
            ArgumentCaptor<Version> versionCaptor = ArgumentCaptor.forClass(Version.class);
            verify(versionRespository)
                    .add(versionCaptor.capture(), eq(fileForBackup));
            assertThat(versionCaptor.getValue().backupTime().backupTime())
                    .isEqualTo(currentTime);
        }

        @Test void backupExistingFile() {
            when(fileRepository.get(fileId))
                    .thenReturn(Optional.of(backupedFile()));

            service.backup(fileId, fileForBackup);

            verify(fileRepository, never()).add(any());
            verifyVersion(fileForBackup);
        }

        @Test void latestVersionHashIsEqual() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(Checksum.forContentOf(fileForBackup.toUri()), 0L);
            when(fileRepository.get(fileId))
                    .thenReturn(Optional.of(backupedFile));

            service.backup(fileId, fileForBackup);

            verify(versionRespository, never()).add(any(), any());
        }
    }

    @Nested
    class BackupIsNecessaryTest {

        @Test void fileDoesntExist() {
            Checksum checksum = new Checksum(10);
            boolean result = service.isBackupNecessarry(fileId, checksum);
            assertThat(result).isTrue();
        }

        @Test void noVersionExists() {
            Checksum checksum = new Checksum(10);
            when(fileRepository.get(fileId))
                    .thenReturn(Optional.of(backupedFile()));

            boolean result = service.isBackupNecessarry(fileId, checksum);

            assertThat(result).isTrue();
        }

        @Test void latestVersionHashIsDifferent() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(new Checksum(20), 0L);
            when(fileRepository.get(fileId))
                    .thenReturn(Optional.of(backupedFile));

            boolean result = service.isBackupNecessarry(fileId, new Checksum(21));

            assertThat(result).isTrue();
        }

        @Test void latestVersionHashIsEqual() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(new Checksum(20), 0L);
            when(fileRepository.get(fileId))
                    .thenReturn(Optional.of(backupedFile));

            boolean result = service.isBackupNecessarry(fileId, new Checksum(20));

            assertThat(result).isFalse();
        }

    }

}
