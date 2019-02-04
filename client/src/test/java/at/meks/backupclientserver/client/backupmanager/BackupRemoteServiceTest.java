package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.MockUtils;
import at.meks.backupclientserver.client.ServerStatusService;
import at.meks.backupclientserver.client.SystemService;
import at.meks.backupclientserver.client.http.HttpUrlResolver;
import at.meks.backupclientserver.client.http.JsonHttpClient;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import at.meks.clientserverbackup.testutils.TestDirectoryProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
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

    @Mock
    private ServerStatusService serverStatusService;

    @Spy
    private JsonHttpClient jsonHttpClient = new JsonHttpClient();

    @InjectMocks
    private BackupRemoteService backupRemoteService = new BackupRemoteService();

    private Path backupSetPath;
    private Path fileForBackup;

    @Before
    public void prepareConfig() throws IOException, IllegalAccessException {
        when(systemService.getHostname()).thenReturn("MEKS-ZENBOOK");
        when(httpUrlResolver.getWebserviceUrl(anyString(), anyString())) .thenAnswer(invocation ->
                format("http://localhost:" + wireMockRule.port() + "/api/v1.0/%s/%s", invocation.getArgument(0), invocation.getArgument(1)));
        backupSetPath = TestDirectoryProvider.createTempDirectory();
        Path pathToBackupFile = Files.createDirectories(backupSetPath.resolve("the").resolve("expected").resolve("path"));
        fileForBackup = prepareFileForTest(pathToBackupFile, "fileToBackup.txt");
        MockUtils.mockDelegate(serverStatusService);
        FieldUtils.writeField(jsonHttpClient, "serverStatusService", serverStatusService, true);
    }

    private Path prepareFileForTest(Path pathToBackupFile, String fileName) throws IOException {
        Path fileForBackup = pathToBackupFile.resolve(fileName);
        Files.copy(getClass().getResourceAsStream(fileName), fileForBackup);
        return fileForBackup;
    }

    @Test
    public void whenBackupFileThenHttpRequestIsInvokedAsExpected() throws IOException {
        byte[] expectedUploadedBytes = FileUtils.readFileToByteArray(fileForBackup.toFile());
        String expectedRelativePath = String.join(", ", getRelativePathOfPackage());
        String hostName = InetAddress.getLocalHost().getHostName();

        stubFor(post("/api/v1.0/backup/file").willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

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
    public void whenIsFileUpToDateThenHttpRequestIsInvokedAsExpected() throws IOException {
        Path fileForUpdateCheck = prepareFileForTest(fileForBackup.getParent(), "up2date.txt");
        String inputString = getExpectedJsonInputString(getFileUp2DateInput(fileForUpdateCheck));
        FileUp2dateInput fileUp2dateInput = aFileUp2dateInput(fileForUpdateCheck);

        stubFor(post("/api/v1.0/backup/isFileUpToDate")
                .withRequestBody(equalToJson(getExpectedJsonInputString(fileUp2dateInput)))
                .willReturn(ResponseDefinitionBuilder.okForJson(new FileUp2dateResult(true))));

        backupRemoteService.isFileUpToDate(backupSetPath, fileForUpdateCheck);

        ArgumentCaptor<FileUp2dateInput> captor = ArgumentCaptor.forClass(FileUp2dateInput.class);
        verify(jsonHttpClient).post(endsWith("/api/v1.0/backup/isFileUpToDate"), captor.capture(), eq(FileUp2dateResult.class));
        FileUp2dateInput up2dateInput = captor.getValue();
        assertThat(up2dateInput.getBackupedPath()).isEqualTo(backupSetPath.toString());
        assertThat(up2dateInput.getFileName()).isEqualTo(fileForUpdateCheck.toFile().getName());
        assertThat(up2dateInput.getHostName()).isEqualTo(InetAddress.getLocalHost().getHostName());
        assertThat(up2dateInput.getRelativePath()).isEqualTo(getRelativePathOfPackage());
        assertThat(up2dateInput.getMd5Checksum()).isEqualTo(getMd5Checksum(fileForUpdateCheck));

        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/isFileUpToDate"))
                .withRequestBody(equalToJson(inputString)));
    }

    private String getMd5Checksum(Path file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            return md5Hex(fis);
        }
    }

    private String[] getRelativePathOfPackage() {
        return StreamSupport.stream(backupSetPath.relativize(fileForBackup.getParent()).spliterator(), false)
                .map(Path::toFile)
                .map(File::getName)
                .toArray(String[]::new);
    }

    private String getExpectedJsonInputString(FileInputArgs input) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private FileUp2dateInput getFileUp2DateInput(Path fileForBackup) throws IOException {
        FileUp2dateInput input = new FileUp2dateInput();
        setFileInputArgProps(fileForBackup, input);
        try (FileInputStream fis = new FileInputStream(fileForBackup.toFile())) {
            input.setMd5Checksum(md5Hex(fis));
        }
        return input;
    }

    private void setFileInputArgProps(Path fileForBackup, FileInputArgs input) {
        try {
            input.setHostName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
        input.setBackupedPath(backupSetPath.toString());
        input.setRelativePath(getRelativePathOfPackage());
        input.setFileName(fileForBackup.toFile().getName());
    }

    @Test
    public void givenUpToDateFileWhenIsFileUpToDateReturnsTrue() throws IOException {
        Path fileForUpdateCheck = prepareFileForTest(fileForBackup.getParent(), "up2date.txt");
        FileUp2dateInput fileUp2dateInput = aFileUp2dateInput(fileForUpdateCheck);
        MappingBuilder mappingBuilder = post("/api/v1.0/backup/isFileUpToDate")
                .withRequestBody(equalToJson(getExpectedJsonInputString(fileUp2dateInput)));
        stubFor(mappingBuilder
                .willReturn(ResponseDefinitionBuilder.okForJson(new FileUp2dateResult(true))));
        boolean result = backupRemoteService.isFileUpToDate(backupSetPath, fileForUpdateCheck);
        assertThat(result).isTrue();
        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/isFileUpToDate"))
                .withRequestBody(equalToJson(getExpectedJsonInputString(fileUp2dateInput))));
    }

    private FileInputArgs getFileInputArgProps(Path fileForBackup) {
        FileInputArgs fileInputArgs = new FileInputArgs();
        setFileInputArgProps(fileForBackup, fileInputArgs);
        return fileInputArgs;
    }

    @Test
    public void givenOutOfDateFileWhenIsFileUpToDateReturnsFalse() throws IOException {
        Path fileForUp2DateCheck = prepareFileForTest(fileForBackup.getParent(), "outOfDate.txt");
        FileUp2dateInput fileUp2dateInput = aFileUp2dateInput(fileForUp2DateCheck);

        stubFor(post("/api/v1.0/backup/isFileUpToDate")
                .willReturn(ResponseDefinitionBuilder.okForJson(new FileUp2dateResult(false))));

        boolean result = backupRemoteService.isFileUpToDate(backupSetPath, fileForUp2DateCheck);

        assertThat(result).isFalse();
        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/isFileUpToDate"))
                .withRequestBody(equalToJson(new ObjectMapper().writeValueAsString(fileUp2dateInput))));
    }

    private FileUp2dateInput aFileUp2dateInput(Path file) throws IOException {
        FileUp2dateInput input = new FileUp2dateInput();
        input.setMd5Checksum(md5Hex(new FileInputStream(file.toFile())));
        setFileInputArgProps(file, input);
        return input;
    }

    @Test
    public void whenDeleteThenJsonClientIsInvokedAsExpected() {
        stubFor(WireMock.delete(urlEqualTo("/api/v1.0/backup/delete"))
                .willReturn(ResponseDefinitionBuilder.okForJson("{\"up2date\":true}"))
                );

        backupRemoteService.delete(backupSetPath, fileForBackup);

        ArgumentCaptor<FileInputArgs> captor = ArgumentCaptor.forClass(FileInputArgs.class);
        verify(jsonHttpClient).delete(endsWith("/api/v1.0/backup/delete"), captor.capture());
        assertThat(captor.getValue().getBackupedPath()).isEqualTo(backupSetPath.toString());
        assertThat(captor.getValue().getFileName()).isEqualTo(fileForBackup.toFile().getName());
        assertThat(captor.getValue().getRelativePath()).isEqualTo(getRelativePathOfPackage());

        FileInputArgs inputArgs = getFileInputArgProps(fileForBackup);
        String inputString = getExpectedJsonInputString(inputArgs);

        WireMock.verify(deleteRequestedFor(urlEqualTo("/api/v1.0/backup/delete"))
                .withRequestBody(equalTo(inputString)));
    }
}