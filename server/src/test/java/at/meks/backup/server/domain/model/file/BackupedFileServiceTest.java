package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BackupedFileServiceTest {

    private static final Path fileForBackup = Path.of("src", "test", "resources", "fileuploads", "file1.txt")
            .toAbsolutePath();
    protected static final FileId FILE_ID = FileId.idFor(ClientId.newId(), new PathOnClient(fileForBackup));

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
            Path fileForBackup = Path.of("src", "test", "resources", "fileuploads", "file1.txt");
            when(fileRepository.add(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            service.backup(FILE_ID, fileForBackup);

            verify(fileRepository)
                    .add(backupedFile());

            verifyVersion(fileForBackup);
        }

        private void verifyVersion(Path fileForBackup) {
            ArgumentCaptor<BackupTime> backupTimeCaptor = ArgumentCaptor.forClass(BackupTime.class);
            verify(versionRespository)
                    .add(eq(FILE_ID), backupTimeCaptor.capture(), eq(fileForBackup));
            assertThat(backupTimeCaptor.getValue().backupTime())
                    .isEqualTo(currentTime);
        }

        @Test void backupExistingFile() {
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile()));

            service.backup(FILE_ID, fileForBackup);

            verify(fileRepository, never()).add(any());
            verifyVersion(fileForBackup);
        }

        @Test void latestVersionHashIsEqual() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(Checksum.forContentOf(fileForBackup.toUri()));
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile));

            service.backup(FILE_ID, fileForBackup);

            verify(versionRespository, never()).add(any(), any(), any());
        }
    }

    @Nested
    class BackupIsNecessaryTest {

        @Test void fileDoesntExist() {
            Checksum checksum = new Checksum(10);
            boolean result = service.isBackupNecessarry(FILE_ID, checksum);
            assertThat(result).isTrue();
        }

        @Test void noVersionExists() {
            Checksum checksum = new Checksum(10);
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile()));

            boolean result = service.isBackupNecessarry(FILE_ID, checksum);

            assertThat(result).isTrue();
        }

        @Test void latestVersionHashIsDifferent() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(new Checksum(20));
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile));

            boolean result = service.isBackupNecessarry(FILE_ID, new Checksum(21));

            assertThat(result).isTrue();
        }

        @Test void latestVersionHashIsEqual() {
            BackupedFile backupedFile = backupedFile();
            backupedFile.versionWasBackedup(new Checksum(20));
            when(fileRepository.get(FILE_ID))
                    .thenReturn(Optional.of(backupedFile));

            boolean result = service.isBackupNecessarry(FILE_ID, new Checksum(20));

            assertThat(result).isFalse();
        }

    }

}
