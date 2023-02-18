package at.meks.backup.server.application.rest.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.Checksum;
import at.meks.backup.server.persistence.file.BackupedFileEntity;
import at.meks.backup.server.persistence.file.version.FileContent;
import at.meks.backup.server.persistence.file.version.VersionDbEntity;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.Matchers;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static at.meks.backup.server.domain.model.file.TestUtils.pathOf;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class FileResourceIT {

    protected static final String FILE_URL_PATH = "/v1/clients/{clientId}/file/{filepath}";

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
        FileContent.deleteAll();
        VersionDbEntity.deleteAll();
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

        private final ClientId clientId = ClientId.existingId("peterParkersMobile");
        private Path fileForBackup;
        private final Path filePath = Paths.get("/root/test.txt");
        private final ZonedDateTime timeBeforeBackup = ZonedDateTime.now();

        @BeforeEach
        void resetRepository() {
            deleteAllFiles();
            fileForBackup = pathOf("/fileuploads/file1.txt");
        }

        @Test
        void newVersionOfNewFile() {
            whenBackup();

            assertThatRepositoryContains(clientId, filePath, fileForBackup);
            assertVersionOfFile();
        }

        private void assertVersionOfFile() {
            Stream<VersionDbEntity> versionen = VersionDbEntity.<VersionDbEntity>findAll().stream();
            assertThat(versionen)
                    .allSatisfy(version -> {
                            assertThat(version.backupTime)
                                    .isAfterOrEqualTo(this.timeBeforeBackup);
                            assertThat(version.id).isNotBlank();
                            assertThat(version.backupedFileEntity).isNotNull();
                    });
            Stream<FileContent> fileContents = FileContent.findAll().stream();
            assertThat(fileContents)
                    .allSatisfy(content -> {
                        assertThat(content.id).isNotBlank();
                        assertThat(content.version).isNotNull();
                        assertThat(content.content.getBinaryStream())
                                .hasSameContentAs(Files.newInputStream(fileForBackup));
                            });

        }

        private void whenBackup() {
            given()
                    .pathParam("clientId", clientId.text())
                    .pathParam("filepath", URLDecoder.decode(filePath.toString(), StandardCharsets.UTF_8))
                    .multiPart(fileForBackup.toFile())
                    .when()
                    .post(FILE_URL_PATH)
                    .then()
                    .statusCode(RestResponse.StatusCode.NO_CONTENT);
        }

        private void assertThatRepositoryContains(ClientId clientId, Path filePath, Path fileForBackup) {
            BackupedFileEntity expectedFile = expectedFile(clientId, filePath, fileForBackup);

            assertThat(BackupedFileEntity.findAll().<BackupedFileEntity>list())
                    .usingRecursiveFieldByFieldElementComparatorOnFields("clientId", "pathOnClient", "latestVersionChecksum")
                    .containsExactly(expectedFile);
        }

        private BackupedFileEntity expectedFile(ClientId clientId, Path filePath, Path fileForBackup) {
            BackupedFileEntity expectedFile = new BackupedFileEntity();
            expectedFile.clientId = clientId.text();
            expectedFile.pathOnClient = new PathOnClient(filePath).asText();
            expectedFile.latestVersionChecksum = Checksum.forContentOf(fileForBackup.toUri()).hash();
            return expectedFile;
        }

        @Test
        void existingFileDifferentChecksum() {
            givenBackupedFile(clientId, filePath, -1L);

            whenBackup();

            assertThatRepositoryContains(clientId, filePath, fileForBackup);
            assertVersionOfFile();
        }

        @Test
        void existingFileSameChecksum() {
            givenBackupedFile(clientId, filePath, Checksum.forContentOf(fileForBackup.toUri()).hash());
            whenBackup();

            assertThatRepositoryContains(clientId, filePath, fileForBackup);
            assertThat(VersionDbEntity.<VersionDbEntity>findAll().stream())
                    .isEmpty();
        }
    }
}