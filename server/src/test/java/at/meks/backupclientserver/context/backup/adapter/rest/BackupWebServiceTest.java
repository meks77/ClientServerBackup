package at.meks.backupclientserver.context.backup.adapter.rest;

import at.meks.backupclientserver.context.infrastructure.Configuration;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
class BackupWebServiceTest {

    public static final String BACKUP_URL = "/api/v1.0/{clientId}/{directory}/{filename}";
    @Inject
    Configuration configuration;

    @BeforeEach
    public void deleteUploadFolder() throws IOException {
        if (Files.exists(configuration.uploadDir())) {
            FileUtils.forceDelete(configuration.uploadDir().toFile());
        }
    }

    @Test
    void testBackupLifecycleAddUpdateDeleteAdd() throws IOException {
        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion1.txt"))
                .when()
                .put(BACKUP_URL,
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        given()
                .accept(ContentType.TEXT)
                .when()
                .get(BACKUP_URL + "/md5Hex",
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(DigestUtils.md5Hex(getClass().getResourceAsStream("fileVersion1.txt"))));

        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion2.txt"))
                .when()
                .put(BACKUP_URL,
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        given()
                .when()
                .delete(BACKUP_URL,
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);


        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion2.txt"))
                .when()
                .put(BACKUP_URL,
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    void testSameFileContentIsBackupedTwice() {
        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion1.txt"))
                .when()
                .put(BACKUP_URL,
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion1.txt"))
                .when()
                .put(BACKUP_URL,
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

}