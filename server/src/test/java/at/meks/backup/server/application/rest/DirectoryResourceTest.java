package at.meks.backup.server.application.rest;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.Directory;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;

@QuarkusTest
public class DirectoryResourceTest {

    public static final String PATH = "/v1/clients/{clientId}/directories/{directory}";
    MemoryDirectoryRespository repository = new MemoryDirectoryRespository();

    @Produces
    MemoryDirectoryRespository repository() {
        return repository;
    }

    @BeforeEach
    void cleanup() {
        repository.clear();
    }

    @Test
    void addNewLinuxDirectory() {
        given()
                .pathParam("clientId", "clientIdX")
                .pathParam("directory", "/home/theuser/Documents")
            .when()
                .post(PATH)
            .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("directory.id", not(emptyString()));

        assertThat(repository.list())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactly(
                        Directory.directoryWasAdded(
                                ClientId.existingId("clientIdX"),
                                new PathOnClient(Paths.get("/home/theuser/Documents"))));
    }

    @Test
    void addNewWindowsDirectory() {
        given()
                .pathParam("clientId", "clientIdX")
                .pathParam("directory", "C:\\Users\\theuser\\Pictures")
            .when()
                .post(PATH)
            .then()
                .statusCode(RestResponse.StatusCode.OK)
                .body("directory.id", not(emptyString()));

        assertThat(repository.list())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactly(
                        Directory.directoryWasAdded(
                                ClientId.existingId("clientIdX"),
                                new PathOnClient(Paths.get("C:\\Users\\theuser\\Pictures"))));
    }


}
