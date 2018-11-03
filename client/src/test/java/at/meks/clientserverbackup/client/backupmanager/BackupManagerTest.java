package at.meks.clientserverbackup.client.backupmanager;

import at.meks.clientserverbackup.client.ApplicationConfig;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingDeque;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;

public class BackupManagerTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ApplicationConfig config;

    @Spy
    private LinkedBlockingDeque<TodoEntry> backupQueue = new LinkedBlockingDeque<>();

    @InjectMocks
    private BackupManager manager = new BackupManager();

    @Before
    public void mockConfig() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        when(config.getServerPort()).thenReturn(wireMockRule.port());
        when(config.getServerHost()).thenReturn("localhost");
        Assertions.assertThat(backupQueue).as("backup queue mock").isNotNull();
        Assertions.assertThat(FieldUtils.readDeclaredField(manager, "backupQueue", true)).as("backupManager.backupQueue").isNotNull();
        Method startMethod = BackupManager.class.getDeclaredMethod("start");
        startMethod.setAccessible(true);
        startMethod.invoke(manager);
    }

    @Test
    public void givenModifiedFileWhenAddForBackupThenHttpServerRequestIsExecutedAsExpected() throws URISyntaxException, IOException, InterruptedException {
        Path uplodatedFilePath = Paths.get(getClass().getResource("/mappings/backup-successfull.json").toURI());
        byte[] expectedUploadedBytes = FileUtils.readFileToByteArray(uplodatedFilePath.toFile());
        manager.addForBackup(new TodoEntry(PathChangeType.MODIFIED,
                uplodatedFilePath,
                Paths.get(getClass().getResource("/").toURI())));
        Mockito.verify(backupQueue, timeout(2000).times(1)).take();
        Thread.sleep(200);
        verify(postRequestedFor(urlEqualTo("/api/v1.0/backup"))
                .withRequestBodyPart(aMultipart("relativePath").withBody(equalTo("mappings")).build())
                .withRequestBodyPart(aMultipart("hostName").withBody(equalTo(InetAddress.getLocalHost().getHostName())).build())
                .withRequestBodyPart(aMultipart("backupedPath").withBody(equalTo(Paths.get(getClass().getResource("/").toURI()).toFile().getAbsolutePath())).build())
                .withRequestBodyPart(aMultipart("file").withBody(binaryEqualTo(expectedUploadedBytes)).build())
        );
    }

}
