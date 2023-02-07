package at.meks.backup.server.application.rest;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.BackupedFile;
import at.meks.backup.server.domain.model.file.Checksum;
import at.meks.backup.server.domain.model.file.FileId;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

@QuarkusTest
class FileResourceTest {

    @Produces
    private final MemoryFileRespository fileRespository = new MemoryFileRespository();

    @BeforeEach
    void cleanup() {
        fileRespository.clear();
    }

    @Nested
    class IsBackupNecessary {
        public static final String PATH = "/v1/clients/{clientId}/file/{filepath}/latestChecksum/{checksum}";

        @Test
        void notExistingFile() {
            ClientId clientId = ClientId.existingId("peterParkersMobile");
            Path filePath = Paths.get("/root/test.txt");
            given()
                    .pathParam("clientId", clientId.text())
                    .pathParam("filepath", URLDecoder.decode(filePath.toString(), StandardCharsets.UTF_8))
                    .pathParam("checksum", 10945L)
                    .when()
                    .get(PATH)
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("backupNecessary", Matchers.equalTo(true));
        }

        @Test
        void existingFileWithSameChecksum() {
            ClientId clientId = ClientId.existingId("peterParkersMobile");
            Path filePath = Paths.get("/root/test.txt");
            long checksum = 10945L;
            givenBackupedFile(clientId, filePath, checksum);
            given()
                    .pathParam("clientId", clientId.text())
                    .pathParam("filepath", URLDecoder.decode(filePath.toString(), StandardCharsets.UTF_8))
                    .pathParam("checksum", checksum)
                    .when()
                    .get(PATH)
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("backupNecessary", Matchers.equalTo(false));
        }

        private void givenBackupedFile(ClientId clientId, Path filePath, long checksum) {
            BackupedFile backupedFile = BackupedFile.newFileForBackup(FileId.idFor(clientId, new PathOnClient(filePath)));
            backupedFile.versionWasBackedup(new Checksum(checksum));
            fileRespository.add(backupedFile);
        }

        @Test
        void existingFileDifferentChecksum() {
            ClientId clientId = ClientId.existingId("peterParkersMobile");
            Path filePath = Paths.get("/root/test.txt");
            long checksum = 10945L;
            givenBackupedFile(clientId, filePath, checksum - 1L);
            given()
                    .pathParam("clientId", clientId.text())
                    .pathParam("filepath", URLDecoder.decode(filePath.toString(), StandardCharsets.UTF_8))
                    .pathParam("checksum", checksum)
                    .when()
                    .get(PATH)
                    .then()
                    .statusCode(RestResponse.StatusCode.OK)
                    .body("backupNecessary", Matchers.equalTo(true));
        }

    }

}