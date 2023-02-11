package at.meks.backup.server.application.rest;

import at.meks.backup.server.domain.model.client.ClientName;
import at.meks.backup.server.domain.model.client.ClientService;
import at.meks.backup.server.persistence.ClientEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.restassured.RestAssured;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@QuarkusTest
class ClientResourceTest {

    @InjectSpy
    ClientService service;

    @BeforeEach
    @Transactional
    void cleanRepo() {
        ClientEntity.deleteAll();
    }

    @Test
    void clientRegistersWithNameNull() {
        RestAssured.given()
                .when().post("/v1/clients/")
                .then()
                .statusCode(RestResponse.StatusCode.NOT_FOUND);
        assertThat(ClientEntity.count())
                .isEqualTo(0L);
    }

    @Test
    void clientRegistersWithName() {
        RestAssured.given()
                .when().post("/v1/clients/myName")
                .then()
                .statusCode(RestResponse.StatusCode.OK);

        verify(service).register(new ClientName("myName"));

        List<ClientEntity> list = ClientEntity.findAll().list();
        assertThat(list)
                .extracting(ClientEntity::name)
                .containsExactly("myName");
    }

}