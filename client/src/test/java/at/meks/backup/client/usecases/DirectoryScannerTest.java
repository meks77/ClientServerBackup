package at.meks.backup.client.usecases;

import at.meks.backup.client.model.Config;
import at.meks.backup.client.model.DirectoryForBackup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectoryScannerTest {

    private static final DirectoryForBackup[] DIRECTORIES = {
            directory("dirOne"),
            directory("dirTwo"),
            directory("dirThree")};

    @Mock
    Config config;

    @Mock
    FileChangeListener fileChangeListener;

    @Mock
    BackupEachFileScanner backupEachFileScanner;

    @InjectMocks
    DirectoryScanner directoryScanner;

    private static DirectoryForBackup directory(String dirOne) {
        return new DirectoryForBackup(Path.of(dirOne));
    }

    @Test
    void fileChangeListenerIsInitializedForEachDirectory() {
        when(config.backupedDirectories())
                .thenReturn(DIRECTORIES);

        directoryScanner.scanDirectories();

        verifyChangeListenerIsInitialized();
    }

    @Test
    void fileScannerIsInitializedForEachDirectory() {
        when(config.backupedDirectories())
                .thenReturn(DIRECTORIES);

        directoryScanner.scanDirectories();

        verifyFileScannerIsInitialized();
    }

    private void verifyChangeListenerIsInitialized() {
        assertThat(argumentValuesOfListener())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(DIRECTORIES);
    }

    private List<DirectoryForBackup> argumentValuesOfListener() {
        ArgumentCaptor<DirectoryForBackup> captor = ArgumentCaptor.forClass(DirectoryForBackup.class);
        verify(fileChangeListener, Mockito.atLeastOnce())
                .listenToChangesAsync(captor.capture());
        return captor.getAllValues();
    }

    private void verifyFileScannerIsInitialized() {
        assertThat(argumentValuesOfScanner())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(DIRECTORIES);}

    private List<DirectoryForBackup> argumentValuesOfScanner() {
        ArgumentCaptor<DirectoryForBackup> captor = ArgumentCaptor.forClass(DirectoryForBackup.class);
        verify(backupEachFileScanner, Mockito.atLeastOnce())
                .fireChangedEventForEachFileAsync(captor.capture());
        return captor.getAllValues();
    }


}
