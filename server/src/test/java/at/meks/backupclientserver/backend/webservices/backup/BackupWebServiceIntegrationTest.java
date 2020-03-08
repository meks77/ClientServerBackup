package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.api.ClientId;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@QuarkusTest
class BackupWebServiceIntegrationTest {

    @Test
    public void testGetAvailableActions() {
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
}