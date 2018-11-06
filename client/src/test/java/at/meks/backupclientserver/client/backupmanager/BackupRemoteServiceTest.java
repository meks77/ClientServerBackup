package at.meks.backupclientserver.client.backupmanager;


import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.client.ApplicationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class BackupRemoteServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ApplicationConfig config;

    @InjectMocks
    private BackupRemoteService backupRemoteService = new BackupRemoteService();
    private Path backupSetPath;
    private Path fileForBackup;

    @Before
    public void prepareConfig() throws URISyntaxException {
        Mockito.when(config.getServerHost()).thenReturn("localhost");
        Mockito.when(config.getServerPort()).thenReturn(wireMockRule.port());
        backupSetPath = Paths.get(getClass().getResource("/").toURI());
        fileForBackup = Paths.get(getClass().getResource("fileToBackup.txt").toURI());
    }

    @Test
    public void whenBackupFileThenHttpRequestIsInvokedAsExpected() throws IOException {
        byte[] expectedUploadedBytes = FileUtils.readFileToByteArray(fileForBackup.toFile());
        String expectedRelativePath = Paths.get("at", "meks", "backupclientserver", "client", "backupmanager").toString();
        String hostName = InetAddress.getLocalHost().getHostName();

        backupRemoteService.backupFile(backupSetPath, fileForBackup);

        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/file"))
                .withRequestBodyPart(aMultipart("relativePath").withBody(equalTo(expectedRelativePath)).build())
                .withRequestBodyPart(aMultipart("hostName").withBody(equalTo(hostName)).build())
                .withRequestBodyPart(aMultipart("backupedPath").withBody(equalTo(backupSetPath.toString())).build())
                .withRequestBodyPart(aMultipart("file").withBody(binaryEqualTo(expectedUploadedBytes)).build())
        );
    }

    @Test
    public void whenIsFileUpToDateThenHttpRequestIsInvokedAsExpected() throws IOException, URISyntaxException {
        Path fileForBackup = Paths.get(getClass().getResource("up2date.txt").toURI());
        String inputString = getExpectedJsonInputString(fileForBackup);

        backupRemoteService.isFileUpToDate(backupSetPath, fileForBackup);

        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/isFileUpToDate"))
                .withRequestBody(equalTo(inputString)));
    }

    private String getExpectedJsonInputString(Path fileForBackup) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        FileUp2dateInput input = getRequestInput(fileForBackup);
        return mapper.writeValueAsString(input);
    }

    private FileUp2dateInput getRequestInput(Path fileForBackup) throws IOException {
        FileUp2dateInput input = new FileUp2dateInput();
        input.setHostName(InetAddress.getLocalHost().getHostName());
        input.setBackupedPath(backupSetPath.toString());
        input.setRelativePath(Paths.get("at", "meks", "backupclientserver", "client", "backupmanager").toString());
        input.setFileName(fileForBackup.toFile().getName());
        try (FileInputStream fis = new FileInputStream(fileForBackup.toFile())) {
            input.setMd5Checksum(DigestUtils.md5Hex(fis));
        }
        return input;
    }

    @Test
    public void givenUpToDateFileWhenIsFileUpToDateReturnsTrue() throws URISyntaxException {
        Path fileForBackup = Paths.get(getClass().getResource("up2date.txt").toURI());
        boolean result = backupRemoteService.isFileUpToDate(backupSetPath, fileForBackup);
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void givenOutOfDateFileWhenIsFileUpToDateReturnsFalse() throws URISyntaxException {
        Path fileForBackup = Paths.get(getClass().getResource("outOfDate.txt").toURI());
        boolean result = backupRemoteService.isFileUpToDate(backupSetPath, fileForBackup);
        Assertions.assertThat(result).isFalse();
    }
}