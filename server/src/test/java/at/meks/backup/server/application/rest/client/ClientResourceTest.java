package at.meks.backup.server.application.rest.client;

import at.meks.backup.server.domain.model.client.ClientName;
import at.meks.backup.server.domain.model.client.ClientService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.restassured.RestAssured;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;

import static org.mockito.Mockito.verify;

@QuarkusTest
class ClientResourceTest {

    @InjectSpy
    ClientService service;

    @Produces
    MemoryClientRepository repository = new MemoryClientRepository();

    @Test
    void clientRegistersWithNameNull() {
        RestAssured.given()
                .when().post("/v1/clients/")
                .then()
                .statusCode(RestResponse.StatusCode.NOT_FOUND);
    }

    @Test
    void clientRegistersWithName() {
        RestAssured.given()
                .when().post("/v1/clients/myName")
                .then()
                .statusCode(RestResponse.StatusCode.OK);

        verify(service).register(new ClientName("myName"));
    }

}