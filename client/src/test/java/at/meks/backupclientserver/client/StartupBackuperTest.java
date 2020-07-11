package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.backup.model.FileChangedEvent;
import at.meks.backupclientserver.client.excludes.FileExcludeService;
import at.meks.backupclientserver.client.startupbackuper.StartupBackuper;
import io.vertx.core.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StartupBackuperTest {

    @Mock
    private EventBus eventBus;

    @TempDir
    Path backupSetPath;

    @Mock
    private SystemService systemService;

    @Mock
    private ErrorReporter errorReporter;

    @Mock
    private FileExcludeService fileExcludeService;

    @InjectMocks
    private StartupBackuper startupBackuper;

    @Test
    public void givenEmptyDirectoryWhenBackupIfNecessarThenEndsWithoutException() {
        startupBackuper.backupIfNecessary(new Path[]{backupSetPath});
        verifyNoInteractions(eventBus);
    }

    @Test
    public void givenDirectoryWithEmptyDirectoryWhenBackupIfNecessaryThenBackupManagerIsNotInvoked() throws IOException {
        createSubFolder(backupSetPath, "subFolder");

        startupBackuper.backupIfNecessary(new Path[]{backupSetPath});
        verifyNoInteractions(eventBus);
    }

    @Test
    public void givenComplexDirectoryStructureWhenBackupIfNecessaryThenFilesAreAddedForBackup() throws IOException {
        createFile(backupSetPath);
        createDirectoryHierarchyWithEmptyFiles(backupSetPath, "subFolder1");
        createDirectoryHierarchyWithEmptyFiles(backupSetPath, "subFolder2");
        createDirectoryHierarchyWithEmptyFiles(backupSetPath, "subFolder3");
        createDirectoryHierarchyWithEmptyFiles(backupSetPath, "subFolder4");

        startupBackuper.backupIfNecessary(new Path[]{backupSetPath});

        ArgumentCaptor<FileChangedEvent> captor = ArgumentCaptor.forClass(FileChangedEvent.class);
        verify(eventBus, times(13)).publish(eq("backup"), captor.capture());

        verifyBackupManagerInvocationsForFolder(captor.getAllValues(), backupSetPath);

    }

    private void verifyBackupManagerInvocationsForFolder(List<FileChangedEvent> actualEntries,  Path backupSetPath) {
        List<Path> expectedFileBackupInvocations = getAllFileRecursive(backupSetPath.toFile());
        assertThat(actualEntries)
                .extracting(FileChangedEvent::changedFile)
                .containsExactlyInAnyOrder(expectedFileBackupInvocations.toArray(new Path[0]));
    }

    private List<Path> getAllFileRecursive(File file) {
        List<Path> fileList = new LinkedList<>();

        File[] directories = file.listFiles(File::isDirectory);
        //noinspection ConstantConditions
        Stream.of(directories).forEach(subDir -> fileList.addAll(getAllFileRecursive(subDir)));

        //noinspection ConstantConditions
        fileList.addAll(Stream.of(file.listFiles(File::isFile)).map(File::toPath).collect(Collectors.toList()));

        return fileList;
    }

    private void createDirectoryHierarchyWithEmptyFiles(Path backupSetPath, String folderName) throws IOException {
        Path folder1 = createSubFolder(backupSetPath, folderName);
        Path folder1SubSubFolder1 = createSubFolder(folder1, "subSubFolder1");
        Path folder1SubSubFolder2 = createSubFolder(folder1, "subSubFolder2");
        createFile(folder1);
        createFile(folder1SubSubFolder1);
        createFile(folder1SubSubFolder2);
    }

    private void createFile(Path targetFolder) throws IOException {
        Files.createTempFile(targetFolder, "watchedFile", ".txt");
    }

    private Path createSubFolder(Path backupSetPath, String subFolder1) throws IOException {
        Path emptySubFolder = backupSetPath.resolve(subFolder1);
        Files.createDirectory(emptySubFolder);
        return emptySubFolder;
    }

    @Test
    public void givenHugeNumberOfNewFilesWhenBackupIfNecessaryThenBackupFinishesWithinTimeout() throws IOException {
        int nrOfFiles = 5000;
        for (int i = 0; i < nrOfFiles; i++) {
            createFile(backupSetPath);
        }

        startupBackuper.backupIfNecessary(new Path[]{backupSetPath});

        verify(eventBus, times(nrOfFiles)).publish(eq("backup"), any());
    }
}
