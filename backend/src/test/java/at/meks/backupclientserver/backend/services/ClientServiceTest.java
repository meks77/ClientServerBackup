package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
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
}
