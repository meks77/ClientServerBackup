package at.meks.backup.client.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)

class DirectoryScannerTest {

    @Mock
    Config config;

    @Mock
    Events events;

    @InjectMocks
    DirectoryScanner directoryScanner;

    @Test
    void moreDirectoriesWithSubdirectoriesAndFiles(@TempDir Path tempDir) {
        Mockito.when(config.backupedDirectories())
                .thenReturn(setup2DirsWithEach2Subdirs(tempDir));
        directoryScanner.scanDirectories();
        verify(events)
                .fireFileChanged(new Events.FileChangedEvent(fileOf(tempDir, "dir1", "testfile.txt")));
        verify(events)
                .fireFileChanged(new Events.FileChangedEvent(fileOf(tempDir, "dir1", "subdir1", "testfile.txt")));
        verify(events)
                .fireFileChanged(new Events.FileChangedEvent(fileOf(tempDir, "dir1", "subdir2", "testfile.txt")));
        verify(events)
                .fireFileChanged(new Events.FileChangedEvent(fileOf(tempDir, "dir2", "testfile.txt")));
        verify(events)
                .fireFileChanged(new Events.FileChangedEvent(fileOf(tempDir, "dir2", "subdir1", "testfile.txt")));
        verify(events)
                .fireFileChanged(new Events.FileChangedEvent(fileOf(tempDir, "dir2", "subdir2", "testfile.txt")));
        verifyNoMoreInteractions(events);
    }

    private Path fileOf(Path tempDir, String... nextOnes) {
        return Path.of(tempDir.toString(), nextOnes);
    }

    private DirectoryForBackup[] setup2DirsWithEach2Subdirs(Path tempDir) {
        return new DirectoryForBackup[] {
                new DirectoryForBackup(createDirWithSubdirs(tempDir, "dir1")),
                new DirectoryForBackup(createDirWithSubdirs(tempDir, "dir2"))};
    }

    private Path createDirWithSubdirs(Path parent, String directoryName) {
        Path dir1 = createDirectoryWithOneFile(parent, directoryName);
        createDirectoryWithOneFile(dir1, "subdir1");
        createDirectoryWithOneFile(dir1, "subdir2");
        return dir1;
    }

    private Path createDirectoryWithOneFile(Path parent, String directoryName) {
        try {
            return createDirectoryWithOneFileThrowingException(parent, directoryName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private Path createDirectoryWithOneFileThrowingException(Path parent, String directoryName) throws IOException {
        Path dir1 = parent.resolve(directoryName);
        Files.createDirectories(dir1);
        Path file = Files.createFile(dir1.resolve("testfile.txt"));
        Files.writeString(file, "hello backup");
        return dir1;
    }

}
