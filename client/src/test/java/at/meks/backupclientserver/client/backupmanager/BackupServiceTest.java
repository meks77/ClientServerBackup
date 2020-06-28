package at.meks.backupclientserver.client.backupmanager;

import at.meks.backupclientserver.client.ServerStatusService;
import at.meks.backupclientserver.client.SystemService;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

//@Ignore("Tests must be better separated")
@ExtendWith(MockitoExtension.class)
public class BackupServiceTest {

    public static final String EXPECTED_HOST = "MEKS-ZENBOOK";
    @Mock
    private SystemService systemService;

    @Mock
    private ServerStatusService serverStatusService;

    @Mock
    private RemoteBackupService remoteBackupService;

    @InjectMocks
    private BackupService backupService = new BackupService();

    @TempDir
    Path backupSetPath;
    private Path fileForBackup;

    @BeforeEach
    public void prepareConfig() throws IOException {
        when(systemService.getHostname()).thenReturn(EXPECTED_HOST);
        Path pathToBackupFile = Files.createDirectories(backupSetPath.resolve("the").resolve("expected").resolve("path"));
        fileForBackup = prepareFileForTest(pathToBackupFile, "fileToBackup.txt");
    }

    private Path prepareFileForTest(Path pathToBackupFile, String fileName) throws IOException {
        Path fileForBackup = pathToBackupFile.resolve(fileName);
        Files.copy(getClass().getResourceAsStream(fileName), fileForBackup);
        return fileForBackup;
    }

    @Test
    public void whenBackupFileThenHttpRequestIsInvokedAsExpected() {
        Response response = mock(Response.class);
        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        doReturn(response).when(remoteBackupService).backupFile(eq(EXPECTED_HOST),
                eq(URLEncoder.encode(fileForBackup.getParent().toString(), StandardCharsets.UTF_8)),
                eq(fileForBackup.getFileName().toString()),
                inputStreamCaptor.capture());
        when(response.getStatus()).thenReturn(Response.Status.NO_CONTENT.getStatusCode());

        backupService.backupFile(fileForBackup);

        //TODO verify that the Inputstream was created for the expected file
    }

    @Disabled("what's better, Integrationtest or mock test of remotebackupservice?")
    @Test
    public void whenIsFileUpToDateThenHttpRequestIsInvokedAsExpected() throws IOException {
        Path fileForUpdateCheck = prepareFileForTest(fileForBackup.getParent(), "up2date.txt");
        String inputString = getExpectedJsonInputString(getFileUp2DateInput(fileForUpdateCheck));
        FileUp2dateInput fileUp2dateInput = aFileUp2dateInput(fileForUpdateCheck);

//        stubFor(post("/api/v1.0/backup/isFileUpToDate")
//                .withRequestBody(equalToJson(getExpectedJsonInputString(fileUp2dateInput)))
//                .willReturn(ResponseDefinitionBuilder.okForJson(new FileUp2dateResult(true))));

        backupService.isFileUpToDate(fileForUpdateCheck);

        ArgumentCaptor<FileUp2dateInput> captor = ArgumentCaptor.forClass(FileUp2dateInput.class);
        // Todo verify remote service invocation
//        verify(jsonHttpClient).post(endsWith("/api/v1.0/backup/isFileUpToDate"), captor.capture(), eq(FileUp2dateResult.class));
        FileUp2dateInput up2dateInput = captor.getValue();
        assertThat(up2dateInput.getBackupedPath()).isEqualTo(backupSetPath.toString());
        assertThat(up2dateInput.getFileName()).isEqualTo(fileForUpdateCheck.toFile().getName());
        assertThat(up2dateInput.getHostName()).isEqualTo(InetAddress.getLocalHost().getHostName());
        assertThat(up2dateInput.getRelativePath()).isEqualTo(getRelativePathOfPackage());
        assertThat(up2dateInput.getMd5Checksum()).isEqualTo(getMd5Checksum(fileForUpdateCheck));

//        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/isFileUpToDate"))
//                .withRequestBody(equalToJson(inputString)));
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

    @Disabled("what's better, Integrationtest or mock test of remotebackupservice?")
    @Test
    public void givenUpToDateFileWhenIsFileUpToDateReturnsTrue() throws IOException {
        Path fileForUpdateCheck = prepareFileForTest(fileForBackup.getParent(), "up2date.txt");
        FileUp2dateInput fileUp2dateInput = aFileUp2dateInput(fileForUpdateCheck);
//        MappingBuilder mappingBuilder = post("/api/v1.0/backup/isFileUpToDate")
//                .withRequestBody(equalToJson(getExpectedJsonInputString(fileUp2dateInput)));
//        stubFor(mappingBuilder
//                .willReturn(ResponseDefinitionBuilder.okForJson(new FileUp2dateResult(true))));
        boolean result = backupService.isFileUpToDate(fileForUpdateCheck);
        assertThat(result).isTrue();
//        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/isFileUpToDate"))
//                .withRequestBody(equalToJson(getExpectedJsonInputString(fileUp2dateInput))));
    }

    private FileInputArgs getFileInputArgProps(Path fileForBackup) {
        FileInputArgs fileInputArgs = new FileInputArgs();
        setFileInputArgProps(fileForBackup, fileInputArgs);
        return fileInputArgs;
    }

    @Disabled("what's better, Integrationtest or mock test of remotebackupservice?")
    @Test
    public void givenOutOfDateFileWhenIsFileUpToDateReturnsFalse() throws IOException {
        Path fileForUp2DateCheck = prepareFileForTest(fileForBackup.getParent(), "outOfDate.txt");
        FileUp2dateInput fileUp2dateInput = aFileUp2dateInput(fileForUp2DateCheck);

//        stubFor(post("/api/v1.0/backup/isFileUpToDate")
//                .willReturn(ResponseDefinitionBuilder.okForJson(new FileUp2dateResult(false))));

        boolean result = backupService.isFileUpToDate(fileForUp2DateCheck);

        assertThat(result).isFalse();
//        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1.0/backup/isFileUpToDate"))
//                .withRequestBody(equalToJson(new ObjectMapper().writeValueAsString(fileUp2dateInput))));
    }

    private FileUp2dateInput aFileUp2dateInput(Path file) throws IOException {
        FileUp2dateInput input = new FileUp2dateInput();
        input.setMd5Checksum(md5Hex(new FileInputStream(file.toFile())));
        setFileInputArgProps(file, input);
        return input;
    }

    @Disabled("what's better, Integrationtest or mock test of remotebackupservice?")
    @Test
    public void whenDeleteThenJsonClientIsInvokedAsExpected() {
//        stubFor(WireMock.delete(urlEqualTo("/api/v1.0/backup/delete"))
//                .willReturn(ResponseDefinitionBuilder.okForJson("{\"up2date\":true}"))
//                );

        backupService.delete(fileForBackup);

        ArgumentCaptor<FileInputArgs> captor = ArgumentCaptor.forClass(FileInputArgs.class);
        // TODO verify remote service call
//        verify(jsonHttpClient).delete(endsWith("/api/v1.0/backup/delete"), captor.capture());
        assertThat(captor.getValue().getBackupedPath()).isEqualTo(backupSetPath.toString());
        assertThat(captor.getValue().getFileName()).isEqualTo(fileForBackup.toFile().getName());
        assertThat(captor.getValue().getRelativePath()).isEqualTo(getRelativePathOfPackage());

        FileInputArgs inputArgs = getFileInputArgProps(fileForBackup);
        String inputString = getExpectedJsonInputString(inputArgs);

//        WireMock.verify(deleteRequestedFor(urlEqualTo("/api/v1.0/backup/delete"))
//                .withRequestBody(equalTo(inputString)));
    }
}