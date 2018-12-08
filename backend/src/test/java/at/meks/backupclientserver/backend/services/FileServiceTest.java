package at.meks.backupclientserver.backend.services;

import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.assertj.core.data.Offset;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FileServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DirectoryService directoryService;

    @InjectMocks
    private FileService service = new FileService();

    @Test
    public void getFreeSpaceMb() throws IOException {
        Path rootBackupDir = TestDirectoryProvider.createTempDirectory();
        Path dir2 = Files.createDirectories(rootBackupDir.resolve("dir2"));
        Random random = new Random();
        Path[] direcotries = new Path[]{
                Files.createDirectories(rootBackupDir.resolve("dir1")),
                dir2,
                Files.createDirectories(dir2.resolve("subdir2-1")),
                Files.createDirectories(rootBackupDir.resolve("dir3"))};
        int nrOfFiles = random.nextInt(20);
        int[] fileSizes = new int[nrOfFiles];
        for (int i = 0; i < fileSizes.length; i++) {
            fileSizes[i] = random.nextInt(1024 * 1024 * 10) + 1;
            createFile(direcotries[random.nextInt(direcotries.length)], fileSizes[i]);
        }
        long expectedUsedSpaceBytes = Arrays.stream(fileSizes).mapToLong(value -> (long) value).sum();

        long beforeServiceCall = System.currentTimeMillis();

        when(directoryService.getBackupRootDirectory()).thenReturn(rootBackupDir);

        FileStatistics statistics = service.getBackupFileStatistics();

        long afterServiceCall = System.currentTimeMillis();
        long serviceCallDuration = afterServiceCall - beforeServiceCall;
        assertThat(statistics.getSizeInMb().doubleValue()).isEqualTo(expectedUsedSpaceBytes/1024.0/1024.0, Offset.offset(0.1));
        assertThat(statistics.getFileCount()).isEqualTo(fileSizes.length);
        assertThat(serviceCallDuration).isLessThan(1000L);
    }

    private void createFile(Path path, long fileSize) throws IOException {
        Path tempFile = Files.createTempFile(path, "ut-file", ".txt");
        new RandomAccessFile(tempFile.toFile(), "rw").setLength(fileSize);
    }

}
