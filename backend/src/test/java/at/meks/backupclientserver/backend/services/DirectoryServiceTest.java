package at.meks.backupclientserver.backend.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.Assertions;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryServiceTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public SpringMethodRule repeatRule = new SpringMethodRule();

    @Mock
    private BackupConfiguration configuration;

    @InjectMocks
    private DirectoryService service;

    @Test
    @Repeat(10)
    public void givenManyThreadsForSameHostAndBackupSetPathWhenGetBackupSetPathThenNoExceptionIsThrown() throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("utThreading");
        String clientBackupSetPath = "C:\\backup\\set\\path";

        when(configuration.getApplicationRootDirectory()).thenReturn(tempDir);

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        String clientHostName = "utHostName";
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> service.getBackupSetPath(clientHostName, clientBackupSetPath));
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.MINUTES);
        assertThat(getExpectedPath(tempDir, clientBackupSetPath, clientHostName)).exists();
    }

    @Test
    public void givenHostNameAndBackupSetPathWhenGetBackupSetPathReturnsExpected() throws IOException {
        Path tempDir = Files.createTempDirectory("utBckstPth");
        String clientBackupSetPath = "C:\\backup\\set\\path";
        String clientHostName = "utHostName";

        when(configuration.getApplicationRootDirectory()).thenReturn(tempDir);

        service.getBackupSetPath(clientHostName, clientBackupSetPath);
        assertThat(getExpectedPath(tempDir, clientBackupSetPath, clientHostName)).exists();
    }

    private Path getExpectedPath(Path tempDir, String clientBackupSetPath, String clientHostName) {
        return Paths.get(tempDir.toString(), md5Hex(clientHostName), md5Hex(clientBackupSetPath));
    }
}
