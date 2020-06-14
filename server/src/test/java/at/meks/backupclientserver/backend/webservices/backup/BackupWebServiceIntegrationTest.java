package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.api.ClientId;
import at.meks.backupclientserver.common.service.BackupCommandArgs;
import at.meks.backupclientserver.common.service.backup.FileStatus;
import at.meks.backupclientserver.common.service.backup.WebBackupAction;
import at.meks.backupclientserver.common.service.backup.WebLink;
import at.meks.backupclientserver.common.service.backup.WebMethod;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@QuarkusTest
class BackupWebServiceIntegrationTest {

    @ConfigProperty(name = "application.root.dir")
    private String rootDir;

    @ConfigProperty(name = "application.upload.dir")
    private String uploadDir;

    @Inject
    FileUploadService fileUploadService;

    @Test
    void testGetAvailableActions() {
        final FileUp2dateInput input = new FileUp2dateInput();
        input.setMd5Checksum("myChecksum");
        input.setBackupedPath("C://whatever");
        input.setFileName("myUtFilename.txt");
        input.setHostName("utHostName");
        input.setRelativePath(new String[]{"whenever", "wherever"});
        input.setClientId(new ClientId("utClientId"));
        final Response response = RestAssured.given()
                .body(input)
                .contentType(ContentType.JSON)
                .when().post("/api/v1.0/backup/fileStatus");
        final FileStatus actions = response.as(FileStatus.class);

        assertThat(actions.getLinks())
                .containsOnlyOnce(
                        new WebLink(WebBackupAction.INITIAL_BACKUP, "/api/v1.0/backup", WebMethod.POST),
                        new WebLink(WebBackupAction.UPLOAD, "/api/v1.0/file", WebMethod.POST));
    }

    @SneakyThrows
    @Test
    void testBackup(@TempDir Path tempDir) {
        final Path file = Files.createTempFile(tempDir, "ut", ".tmp");
        Files.write(file, UUID.randomUUID().toString().getBytes());
        final String uploadedFilePath = fileUploadService.upload(Files.newInputStream(file));

        BackupCommandArgs backupCommandArgs = new BackupCommandArgs();
        backupCommandArgs.setBackupedPath("/my/backuped/dir");
        backupCommandArgs.setFileName(UUID.randomUUID().toString());
        backupCommandArgs.setRelativePath(new String[] {"a", "b", "c"});
        backupCommandArgs.setHostName("MyHostName");
        backupCommandArgs.setClientId("MyClientId");
        backupCommandArgs.setRelativePathUplodadedFile(uploadedFilePath);
        RestAssured.given().body(backupCommandArgs)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/v1.0/backup")
                .then().statusCode(204);
    }
}