package at.meks.backup.server.domain.model.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @InjectMocks
    ClientService service;

    @Mock
    ClientRepository repository;

    @Test
    void clientIsNotRegistered() {
        final ClientName expectedResult = new ClientName("Bruce' Buddy");
        service.register(expectedResult);

        assertThat(registeredName())
                .isEqualTo(expectedResult);
    }

    private ClientName registeredName() {
        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(repository)
                .add(clientCaptor.capture());
        return clientCaptor.getValue().name();
    }


}
