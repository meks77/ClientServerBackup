package at.meks.backup.server.domain.model.client;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @InjectMocks
    ClientService service;

    @Mock
    ClientRepository repository;
    private ClientId clientId;
    private ClientName clientName;

    @Test
    void clientIsNotRegistered() {
        givenClientId("adsf");
        givenClientName("Bruce' Buddy");

        when_register();

        verify(repository)
                .create(new Client(clientId, clientName));
    }

    @Test
    void clientIsAlreadyRegistered() {
        givenClientId("4711");
        givenClientName("Bruce' Supercomputer");
        when(repository.find(clientId))
                .thenReturn(Optional.of(new Client(clientId, clientName)));

        assertThatThrownBy(this::when_register)
                .isInstanceOf(ClientAlreadyRegistered.class)
                .hasMessage("Client with id 4711 is already registered. Please choose a different id for registration");
    }

    private void givenClientId(String idText) {
        clientId = new ClientId(idText);
    }

    private void givenClientName(String clientName) {
        this.clientName = new ClientName(clientName);
    }

    @SneakyThrows
    private void when_register() {
        service.register(clientId, clientName);
    }

}
