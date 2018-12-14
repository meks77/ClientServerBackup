package at.meks.backupclientserver.client.backupmanager;


import at.meks.backupclientserver.client.SystemService;
import at.meks.backupclientserver.client.http.HttpUrlResolver;
import at.meks.backupclientserver.client.http.JsonHttpClient;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BackupRemoteServiceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private SystemService systemService;

    @Mock
    private HttpUrlResolver httpUrlResolver;

    @Spy
    private JsonHttpClient jsonHttpClient = new JsonHttpClient();

    @InjectMocks
    private BackupRemoteService backupRemoteService = new BackupRemoteService();

    private Path backupSetPath;
    private Path fileForBackup;

    @Before
    public void prepareConfig() throws URISyntaxException {
        when(systemService.getHostname()).thenReturn("MEKS-ZENBOOK");
        when(httpUrlResolver.getWebserviceUrl(anyString(), anyString())) .thenAnswer(invocation ->
                format("http://localhost:" + wireMockRule.port() + "/api/v1.0/%s/%s", invocation.getArgument(0), invocation.getArgument(1)));
        backupSetPath = Paths.get(getClass().getResource("/").toURI());
        fileForBackup = Paths.get(getClass().getResource("fileToBackup.txt").toURI());
    }

    @Test
    public void whenBackupFileThenHttpRequestIsInvokedAsExpected() throws IOException {
        byte[] expectedUploadedBytes = FileUtils.readFileToByteArray(fileForBackup.toFile());
        String expectedRelativePath = String.join(", ", getRelativePathOfPackage());
        String hostName = InetAddress.getLocalHost().getHostName();

        backupRemoteService.backupFile(backupSetPath, fileForBackup);

        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/file"))
                .withRequestBodyPart(aMultipart("relativePath")
                        .withHeader("Content-Type", equalTo("text/plain; charset=UTF-8"))
                        .withBody(equalTo(expectedRelativePath)).build())
                .withRequestBodyPart(aMultipart("hostName")
                        .withHeader("Content-Type", equalTo("text/plain; charset=UTF-8"))
                        .withBody(equalTo(hostName)).build())
                .withRequestBodyPart(aMultipart("backupedPath")
                        .withHeader("Content-Type", equalTo("text/plain; charset=UTF-8"))
                        .withBody(equalTo(backupSetPath.toString())).build())
                .withRequestBodyPart(aMultipart("fileName")
                        .withHeader("Content-Type", equalTo("text/plain; charset=UTF-8"))
                        .withBody(equalTo(fileForBackup.toFile().getName())).build())
                .withRequestBodyPart(aMultipart("file").withBody(binaryEqualTo(expectedUploadedBytes)).build())
        );
    }

    @Test
    public void whenIsFileUpToDateThenHttpRequestIsInvokedAsExpected() throws IOException, URISyntaxException {
        Path fileForBackup = Paths.get(getClass().getResource("up2date.txt").toURI());
        String inputString = getExpectedJsonInputString(getFileUp2DateInput(fileForBackup));

        backupRemoteService.isFileUpToDate(backupSetPath, fileForBackup);

        ArgumentCaptor<FileUp2dateInput> captor = ArgumentCaptor.forClass(FileUp2dateInput.class);
        verify(jsonHttpClient).post(endsWith("/api/v1.0/backup/isFileUpToDate"), captor.capture(), eq(FileUp2dateResult.class));
        FileUp2dateInput up2dateInput = captor.getValue();
        assertThat(up2dateInput.getBackupedPath()).isEqualTo(backupSetPath.toString());
        assertThat(up2dateInput.getFileName()).isEqualTo(fileForBackup.toFile().getName());
        assertThat(up2dateInput.getHostName()).isEqualTo(InetAddress.getLocalHost().getHostName());
        assertThat(up2dateInput.getRelativePath()).isEqualTo(getRelativePathOfPackage());
        assertThat(up2dateInput.getMd5Checksum()).isEqualTo(getMd5Checksum(fileForBackup));

        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/isFileUpToDate"))
                .withRequestBody(equalTo(inputString)));
    }

    private String getMd5Checksum(Path file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            return DigestUtils.md5Hex(fis);
        }
    }

    private String[] getRelativePathOfPackage() {
        return new String[]{"at", "meks", "backupclientserver", "client", "backupmanager"};
    }

    private String getExpectedJsonInputString(FileInputArgs input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(input);
    }

    private FileUp2dateInput getFileUp2DateInput(Path fileForBackup) throws IOException {
        FileUp2dateInput input = new FileUp2dateInput();
        setFileInputArgProps(fileForBackup, input);
        try (FileInputStream fis = new FileInputStream(fileForBackup.toFile())) {
            input.setMd5Checksum(DigestUtils.md5Hex(fis));
        }
        return input;
    }

    private void setFileInputArgProps(Path fileForBackup, FileInputArgs input) throws UnknownHostException {
        input.setHostName(InetAddress.getLocalHost().getHostName());
        input.setBackupedPath(backupSetPath.toString());
        input.setRelativePath(getRelativePathOfPackage());
        input.setFileName(fileForBackup.toFile().getName());
    }

    @Test
    public void givenUpToDateFileWhenIsFileUpToDateReturnsTrue() throws URISyntaxException {
        Path fileForBackup = Paths.get(getClass().getResource("up2date.txt").toURI());
        boolean result = backupRemoteService.isFileUpToDate(backupSetPath, fileForBackup);
        assertThat(result).isTrue();
    }

    @Test
    public void givenOutOfDateFileWhenIsFileUpToDateReturnsFalse() throws URISyntaxException {
        Path fileForBackup = Paths.get(getClass().getResource("outOfDate.txt").toURI());
        boolean result = backupRemoteService.isFileUpToDate(backupSetPath, fileForBackup);
        assertThat(result).isFalse();
    }

    @Test
    public void whenDeleteThenJsonClientIsInvokedAsExpected() throws IOException {
        backupRemoteService.delete(backupSetPath, fileForBackup);

        ArgumentCaptor<FileInputArgs> captor = ArgumentCaptor.forClass(FileInputArgs.class);
        verify(jsonHttpClient).delete(endsWith("/api/v1.0/backup/delete"), captor.capture());
        assertThat(captor.getValue().getBackupedPath()).isEqualTo(backupSetPath.toString());
        assertThat(captor.getValue().getFileName()).isEqualTo(fileForBackup.toFile().getName());
        assertThat(captor.getValue().getRelativePath()).isEqualTo(getRelativePathOfPackage());

        FileInputArgs inputArgs = new FileInputArgs();
        setFileInputArgProps(fileForBackup, inputArgs);
        String inputString = getExpectedJsonInputString(inputArgs);

        WireMock.verify(deleteRequestedFor(urlEqualTo("/api/v1.0/backup/delete"))
                .withRequestBody(equalTo(inputString)));
    }
}