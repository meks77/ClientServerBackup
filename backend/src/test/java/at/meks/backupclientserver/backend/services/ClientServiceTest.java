package at.meks.backupclientserver.backend.services;

import at.meks.clientserverbackup.testutils.DateTestUtils;
import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
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
        when(repository.getClientCount()).thenReturn(expectedResult);

        assertThat(service.getClientCount()).isEqualTo(expectedResult);
    }

    @Test
    public void givenClientWithNullUpdateTimeWhenUpdateLastBackupTimestampThenClientIsUpdatedWithCurrentTime() {
        String expectedHostName = "expectedHostName";
        Client client = new Client();
        Date timeBeforeCall = new Date();

        when(repository.getClient(expectedHostName)).thenReturn(Optional.of(client));
        service.updateLastBackupTimestamp(expectedHostName);

        verify(repository).update(same(client));
        assertThat(client.getLastBackupedFileTimestamp()).isAfterOrEqualsTo(timeBeforeCall);
    }

    @Test
    public void givenClientWithOldUpdateTimeWhenUpdateLastBackupTimestampThenClientIsUpdatedWithCurrentTime() {
        String expectedHostName = "expectedHostName";
        Client client = new Client();
        Date olderDate = DateTestUtils.fromLocalDateTime(LocalDateTime.now().minusHours(1));
        client.setLastBackupedFileTimestamp(olderDate);
        Date timeBeforeCall = new Date();

        when(repository.getClient(expectedHostName)).thenReturn(Optional.of(client));
        service.updateLastBackupTimestamp(expectedHostName);

        verify(repository).update(same(client));
        assertThat(client.getLastBackupedFileTimestamp()).isAfterOrEqualsTo(timeBeforeCall);
    }
}
