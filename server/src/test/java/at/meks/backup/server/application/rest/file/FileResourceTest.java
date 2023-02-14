package at.meks.backup.server.application.rest.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.Checksum;
import at.meks.backup.server.persistence.file.BackupedFileEntity;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;
import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class FileResourceTest {

    protected static final String FILE_URL_PATH = "/v1/clients/{clientId}/file/{filepath}";

    @Produces
    private final MemoryVersionRepository versionRepository = new MemoryVersionRepository();

    @Transactional
    void givenBackupedFile(ClientId clientId, Path filePath, long checksum) {
        BackupedFileEntity entity = new BackupedFileEntity();
        entity.id = UUID.randomUUID().toString();
        entity.clientId = clientId.text();
        entity.pathOnClient = new PathOnClient(filePath).asText();
        entity.latestVersionChecksum = checksum;
        entity.persist();
    }

    @Transactional
    void deleteAllFiles() {
        BackupedFileEntity.deleteAll();
    }

    @Nested
    class IsBackupNecessary {

        public static final String PATH = FILE_URL_PATH + "/latestChecksum/{checksum}";

        @BeforeEach
        void cleanup() {
            deleteAllFiles();
        }

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

    @Nested
    class Backup {

        @BeforeEach
        void resetRepository() {
            versionRepository.clear();
            deleteAllFiles();
        }

        @Test
        void newVersionOfNewFile() {
            ZonedDateTime timeBeforeBackup = ZonedDateTime.now();
            ClientId clientId = ClientId.existingId("peterParkersMobile");
            Path filePath = Paths.get("/root/test.txt");
            Path fileForBackup = Path.of("build", "resources", "test", "fileuploads", "file1.txt").toAbsolutePath();
            given()
                    .pathParam("clientId", clientId.text())
                    .pathParam("filepath", URLDecoder.decode(filePath.toString(), StandardCharsets.UTF_8))
                    .multiPart(fileForBackup.toFile())
                    .when()
                    .post(FILE_URL_PATH)
                    .then()
                    .statusCode(RestResponse.StatusCode.NO_CONTENT);

            BackupedFileEntity expectedFile = new BackupedFileEntity();
            expectedFile.clientId = clientId.text();
            expectedFile.pathOnClient = new PathOnClient(filePath).asText();
            expectedFile.latestVersionChecksum = Checksum.forContentOf(fileForBackup.toUri()).hash();

            assertThat(BackupedFileEntity.findAll().<BackupedFileEntity>list())
                    .usingRecursiveFieldByFieldElementComparatorOnFields("clientId", "pathOnClient", "latestVersionChecksum")
                    .containsExactly(expectedFile);
            assertThat(versionRepository.stream()
                            .filter(version -> version.backuptime().backupTime().isAfter(timeBeforeBackup))
                            .findFirst())
                    .isNotEmpty();
        }

        //TODO: newVersionOfExistingBackup
        //TODO: sameVersionOfExistingBackup
    }
}