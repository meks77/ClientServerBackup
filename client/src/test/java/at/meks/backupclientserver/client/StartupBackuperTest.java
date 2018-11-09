package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.backupmanager.BackupManager;
import at.meks.backupclientserver.client.backupmanager.TodoEntry;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class StartupBackuperTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BackupManager backupManager;

    @InjectMocks
    private StartupBackuper startupBackuper;

    @Test
    public void givenEmptyDirectoryWhenBackupIfNecessarThenEndsWithoutException() {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        startupBackuper.backupIfNecessary(new Path[]{backupSetPath});
        verify(backupManager, timeout(300).times(0)).addForBackup(any());
    }

    @Test
    public void givenDirectoryWithEmptyDirectoryWhenBackupIfNecessaryThenBackupManagerIsNotInvoked() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        createSubFolder(backupSetPath, "subFolder");

        startupBackuper.backupIfNecessary(new Path[]{backupSetPath});
        verify(backupManager, timeout(300).times(0)).addForBackup(any());
    }

    @Test
    public void givenComplexDirectoryStructureWhenBackupIfNecessaryThenFilesAreAddedForBackup() throws IOException {
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();

        createFile(backupSetPath);
        createDirectoryHierarchyWithEmptyFiles(backupSetPath, "subFolder1");
        createDirectoryHierarchyWithEmptyFiles(backupSetPath, "subFolder2");
        createDirectoryHierarchyWithEmptyFiles(backupSetPath, "subFolder3");
        createDirectoryHierarchyWithEmptyFiles(backupSetPath, "subFolder4");

        startupBackuper.backupIfNecessary(new Path[]{backupSetPath});

        ArgumentCaptor<TodoEntry> captor = ArgumentCaptor.forClass(TodoEntry.class);
        verify(backupManager, timeout(3000).times(13)).addForBackup(captor.capture());

        verifyBackupManagerInvocationsForFolder(captor.getAllValues(), backupSetPath);

    }

    private void verifyBackupManagerInvocationsForFolder(List<TodoEntry> actualEntries,  Path backupSetPath) {
        List<Path> expectedFileBackupInvocations = getAllFileRecursive(backupSetPath.toFile());
        assertThat(extractProperty("changedFile").from(actualEntries)).containsOnly(expectedFileBackupInvocations.toArray());
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

    private Path createDirectoryHierarchyWithEmptyFiles(Path backupSetPath, String folderName) throws IOException {
        Path folder1 = createSubFolder(backupSetPath, folderName);
        Path folder1SubSubFolder1 = createSubFolder(folder1, "subSubFolder1");
        Path folder1SubSubFolder2 = createSubFolder(folder1, "subSubFolder2");
        createFile(folder1);
        createFile(folder1SubSubFolder1);
        createFile(folder1SubSubFolder2);
        return folder1;
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
        Path backupSetPath = TestDirectoryProvider.createTempDirectory();
        int nrOfFiles = 5000;
        for (int i = 0; i < nrOfFiles; i++) {
            createFile(backupSetPath);
        }

        startupBackuper.backupIfNecessary(new Path[]{backupSetPath});

        verify(backupManager, timeout(nrOfFiles).times(nrOfFiles)).addForBackup(any());
    }
}
