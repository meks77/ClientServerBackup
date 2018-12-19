package at.meks.backupclientserver.backend.services.backup;

import at.meks.backupclientserver.backend.services.file.DirectoryService;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MetaDataServiceTest {

    private static final String FILE_CONTENT = "theFileContent";
    private static final String TARGET_FILE_NAME = "targetFile.txt";
    private static final String TARGET_FILE_MD_5_FILE_NAME = "targetFile.txt.md5";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DirectoryService directoryService;

    @InjectMocks
    private MetaDataService service;

    @Test
    public void givenFileWhenWriteMd5ChecksumWritesMd5ToExpectedFile() throws IOException {
        Path rootDir = mockMd5Path();

        String fileContent = FILE_CONTENT;
        String expectedMd5 = DigestUtils.md5Hex(fileContent);
        Path target = Paths.get(rootDir.toString(), TARGET_FILE_NAME);
        Files.write(target, fileContent.getBytes());


        service.writeMd5Checksum(target.toFile());

        String md5OfFile = Files.readAllLines(Paths.get(rootDir.toString(), TARGET_FILE_MD_5_FILE_NAME)).get(0);
        assertThat(md5OfFile).isEqualTo(expectedMd5);
    }

    private Path mockMd5Path() {
        Path rootDir = TestDirectoryProvider.createTempDirectory();
        when(directoryService.getMetadataDirectoryPath(any())).thenReturn(rootDir);
        return rootDir;
    }

    @Test
    public void givenEqualMd5WhenIsMd5EqualReturnsTrue() throws IOException {
        Path rootDir = mockMd5Path();

        String md5Cecksum = DigestUtils.md5Hex(FILE_CONTENT);
        Path target = Paths.get(rootDir.toString(), TARGET_FILE_NAME);
        Path md5File = Paths.get(rootDir.toString(), TARGET_FILE_MD_5_FILE_NAME);
        Files.write(md5File, md5Cecksum.getBytes());

        boolean result = service.isMd5Equal(target.toFile(), md5Cecksum);
        assertThat(result).isTrue();
    }

    @Test
    public void givenNotEqualMd5WhenIsMd5EqualReturnsFalse() throws IOException {
        Path rootDir = mockMd5Path();

        String md5Cecksum = DigestUtils.md5Hex(FILE_CONTENT);
        Path target = Paths.get(rootDir.toString(), TARGET_FILE_NAME);
        Path md5File = Paths.get(rootDir.toString(), TARGET_FILE_MD_5_FILE_NAME);
        Files.write(md5File, md5Cecksum.getBytes());

        boolean result = service.isMd5Equal(target.toFile(), md5Cecksum+"x");
        assertThat(result).isFalse();
    }

    @Test
    public void givenNotExistingTargetWhenIsMd5EqalsReturnsFalse() {
        Path rootDir = mockMd5Path();

        boolean result = service.isMd5Equal(Paths.get(rootDir.toString(), "notExistingFile.txt").toFile(),
                "whatever");
        assertThat(result).isFalse();
    }
}
