package at.meks.backupclientserver.context.backup.adapter.rest;

import at.meks.backupclientserver.context.infrastructure.Configuration;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BackupWebServiceTest {

    @Inject
    Configuration configuration;

    @BeforeEach
    public void deleteUploadFolder() throws IOException {
        FileUtils.forceDelete(configuration.uploadDir().toFile());
    }

    @Test
    void testBackupLifecycleAddUpdateDeleteAdd()  {
        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion1.txt"))
                .when()
                .post("/api/v1.0/backup/{clientId}/{directory}/{filename}",
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion2.txt"))
                .when()
                .put("/api/v1.0/backup/{clientId}/{directory}/{filename}",
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        given()
                .when()
                .delete("/api/v1.0/backup/{clientId}/{directory}/{filename}",
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);


        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion2.txt"))
                .when()
                .put("/api/v1.0/backup/{clientId}/{directory}/{filename}",
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
                .put("/api/v1.0/backup/{clientId}/{directory}/{filename}",
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        given()
                .contentType(ContentType.BINARY)
                .body(getClass().getResourceAsStream("fileVersion1.txt"))
                .when()
                .put("/api/v1.0/backup/{clientId}/{directory}/{filename}",
                        "testclient", "/home/user1/documents", "testFile.txt")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

}

