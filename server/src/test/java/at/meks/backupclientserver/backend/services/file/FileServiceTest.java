package at.meks.backupclientserver.backend.services.file;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.io.FileUtils;
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
import java.util.Arrays;
import java.util.Random;
import java.util.Spliterator;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DirectoryService directoryService;

    @InjectMocks
    private FileService service = new FileService();

    @Test
    public void whenGetBackupFileStatisticsThenReturnsExpectedResult() throws IOException {
        Path rootBackupDir = TestDirectoryProvider.createTempDirectory();
        int[] fileSizes = getFileSizesOfCreatedClientDirectory(rootBackupDir);
        double expectedUsedSpaceMb = getFileSizeSumInMb(fileSizes);

        long beforeServiceCall = System.currentTimeMillis();

        when(directoryService.getBackupRootDirectory()).thenReturn(rootBackupDir);

        FileStatistics statistics = service.getBackupFileStatistics();

        long afterServiceCall = System.currentTimeMillis();
        long serviceCallDuration = afterServiceCall - beforeServiceCall;
        assertThat(statistics.getSizeInMb().doubleValue()).isEqualTo(expectedUsedSpaceMb, Offset.offset(0.1));
        assertThat(statistics.getFileCount()).isEqualTo(fileSizes.length);
        assertThat(serviceCallDuration).isLessThan(1000L);
    }

    private double getFileSizeSumInMb(int[] fileSizes) {
        return Arrays.stream(fileSizes).mapToLong(value -> (long) value).sum() / 1024.0 / 1024.0;
    }

    private int[] getFileSizesOfCreatedClientDirectory(Path rootBackupDir) throws IOException {
        Path dir2 = Files.createDirectories(rootBackupDir.resolve("dir2"));
        Random random = new Random();
        Path[] directories = new Path[]{
                Files.createDirectories(rootBackupDir.resolve("dir1")),
                dir2,
                Files.createDirectories(dir2.resolve("subdir2-1")),
                Files.createDirectories(rootBackupDir.resolve("dir3"))};
        int nrOfFiles = random.nextInt(20);
        int[] fileSizes = new int[nrOfFiles];
        for (int i = 0; i < fileSizes.length; i++) {
            fileSizes[i] = random.nextInt(1024 * 1024 * 10) + 1;
            createFile(directories[random.nextInt(directories.length)], fileSizes[i]);
        }
        return fileSizes;
    }

    private void createFile(Path path, long fileSize) throws IOException {
        Path tempFile = Files.createTempFile(path, "ut-file", ".txt");
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile.toFile(), "rw")) {
            randomAccessFile.setLength(fileSize);
        }
    }

    @Test
    public void givenClientWhenGetDiskUsage() throws IOException {
        Path rootBackupDir = TestDirectoryProvider.createTempDirectory();
        getFileSizesOfCreatedClientDirectory(rootBackupDir);
        Path clientDir = rootBackupDir.resolve("dir2");
        double expectedUsedSpaceMb = FileUtils.sizeOfDirectory(clientDir.toFile()) / 1024.0 / 1024.0;
        Client client = mock(Client.class);

        when(directoryService.getBackupRootDirectory()).thenReturn(rootBackupDir);
        when(directoryService.getClientPath(client)).thenReturn(clientDir);

        FileStatistics statistics = service.getDiskUsage(client);

        assertThat(statistics.getSizeInMb().doubleValue()).isEqualTo(expectedUsedSpaceMb, Offset.offset(0.1));
        long expectedFileCount = stream(spliteratorUnknownSize(iterateFiles(clientDir.toFile(), null, true), Spliterator.ORDERED)
                , false).count();
        assertThat(statistics.getFileCount()).isEqualTo(expectedFileCount);
    }

}
