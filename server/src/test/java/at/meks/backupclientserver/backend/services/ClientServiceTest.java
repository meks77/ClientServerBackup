package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import at.meks.clientserverbackup.testutils.DateTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static at.meks.clientserverbackup.testutils.DateTestUtils.fromLocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClientServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private ClientService service = new ClientService();

    @Test
    public void whenGetClientCountThenResultOfClientRepositoryIsReturned() {
        int expectedResult = 87;
        when(repository.getSize()).thenReturn(expectedResult);

        assertThat(service.getClientCount()).isEqualTo(expectedResult);
    }

    @Test
    public void givenClientWithNullUpdateTimeWhenUpdateLastBackupTimestampThenClientIsUpdatedWithCurrentTime() {
        String expectedHostName = "expectedHostName";
        Client client = Client.aClient().build();
        Date timeBeforeCall = new Date();

        when(repository.getById(expectedHostName)).thenReturn(Optional.of(client));
        service.updateLastBackupTimestamp(expectedHostName);

        verify(repository).update(same(client));
        assertThat(client.getLastBackupedFileTimestamp()).isAfterOrEqualsTo(timeBeforeCall);
    }

    @Test
    public void givenClientWithOldUpdateTimeWhenUpdateLastBackupTimestampThenClientIsUpdatedWithCurrentTime() {
        String expectedHostName = "expectedHostName";
        Client client = Client.aClient().build();
        Date olderDate = fromLocalDateTime(LocalDateTime.now().minusHours(1));
        client.setLastBackupedFileTimestamp(olderDate);
        Date timeBeforeCall = new Date();

        when(repository.getById(expectedHostName)).thenReturn(Optional.of(client));
        service.updateLastBackupTimestamp(expectedHostName);

        verify(repository).update(same(client));
        assertThat(client.getLastBackupedFileTimestamp()).isAfterOrEqualsTo(timeBeforeCall);
    }

    @Test
    public void givenClientFoundWithoutHeartbeatTimestampWhenUpdateHeartbeatThenClientsHeartbeatIsUpdated() {
        givenClientFoundWhenUpdateHeartbeatThenClientsHeartbeatIsUpdated(Client.aClient().build());
    }

    private void givenClientFoundWhenUpdateHeartbeatThenClientsHeartbeatIsUpdated(Client client) {
        Optional<Client> clientOptional = Optional.of(client);
        String hostName = "theUtHostName";
        when(repository.getById(hostName)).thenReturn(clientOptional);
        Date minimumTimestamp = new Date();

        service.updateHeartbeat(hostName);

        verify(repository).update(same(clientOptional.get()));
        assertThat(clientOptional.get().getHeartbeatTimestamp()).isAfterOrEqualsTo(minimumTimestamp);
    }

    @Test
    public void givenClientFoundWithHeartbeatTimestampWhenUpdateHeartbeatThenClientsHeartbeatIsUpdated() {
        givenClientFoundWhenUpdateHeartbeatThenClientsHeartbeatIsUpdated(
                Client.aClient().heartbeatTimestamp(DateTestUtils
                        .fromLocalDateTime(LocalDateTime.of(2006, 8, 7, 1, 2, 3))).build());
    }

    @Test
    public void givenClientNotFoundWhenUpdateHeartbeatThenNoExceptionIsThrown() {
        service.updateHeartbeat("theUtHostName");
        verify(repository, never()).update(any());
    }
}
