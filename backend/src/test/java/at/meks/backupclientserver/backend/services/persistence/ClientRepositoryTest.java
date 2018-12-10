package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.LockService;
import io.jsondb.JsonDBTemplate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ClientRepositoryTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private JsonDBTemplate dbTemplate;

    @Mock
    private LockService lockService;

    @Mock
    private ReentrantLock createNewClientLock;

    @InjectMocks
    private ClientRepository repository;

    @Before
    public void initDefaults() {
        when(persistenceService.getJsonDBTemplate()).thenReturn(dbTemplate);
    }

    @Test
    public void whenInitDbThenJsonDbCollectionIsCreated() {
        repository.initDb();
        verify(dbTemplate).createCollection(Client.class);
    }

    @Test
    public void testThatInitDbIsAnnotatedWithInject() throws NoSuchMethodException {
        assertThat(ClientRepository.class.getMethod("initDb").isAnnotationPresent(Inject.class)).isTrue();
    }

    @Test
    public void givenExistingClientWhenGetClientThenOptionalWithValueIsReturned() {
        String hostName = "theUtHostName";
        Client expectedClient = mock(Client.class);
        when(dbTemplate.findById(hostName, Client.class)).thenReturn(expectedClient);

        Optional<Client> result = repository.getClient(hostName);

        assertThat(result).hasValue(expectedClient);
    }

    @Test
    public void givenNotExistingClientWhenGetClientThenEmptyOptionalIsReturned() {
        String hostName = "theUtHostName";
        when(persistenceService.getJsonDBTemplate()).thenReturn(dbTemplate);
        when(dbTemplate.findById(hostName, Client.class)).thenReturn(null);

        Optional<Client> result = repository.getClient(hostName);

        assertThat(result).isEmpty();
    }

    @Test
    public void whenCreateNewClientThenCreationIsInvokedWithinLock() {
        String hostName = "hostNameForUt";
        String directoryName = "dirNameForHostName";

        when(lockService.runWithLock(eq(createNewClientLock), any()))
                .thenAnswer(invocationOnMock -> ((Supplier<?>)invocationOnMock.getArgument(1)).get());

        repository.createNewClient(hostName, directoryName);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(dbTemplate).insert(captor.capture());
        Client createdClient = captor.getValue();
        assertThat(createdClient.getName()).isEqualTo(hostName);
        assertThat(createdClient.getDirectoryName()).isEqualTo(directoryName);
    }

    @Test
    public void givenExistingClientThenClientIsNotCreated() {
        String hostName = "hostNameForUt";
        String directoryName = "dirNameForHostName";

        when(dbTemplate.findById(hostName, Client.class)).thenReturn(new Client());
        when(lockService.runWithLock(eq(createNewClientLock), any()))
                .thenAnswer(invocationOnMock -> ((Supplier<?>)invocationOnMock.getArgument(1)).get());

        repository.createNewClient(hostName, directoryName);

        verify(dbTemplate, never()).insert(any());
    }

    @Test
    public void whenUpdateThenDbTemplateIsInvoked() {
        Client client = mock(Client.class);
        repository.update(client);

        verify(dbTemplate).save(client, Client.class);
    }

    @Test
    public void whenGetClientCountThenSizeOfDbTemplateCollectionsIsReturned() {
        int expectedResult = 74;
        when(dbTemplate.getCollection(Client.class)).thenReturn(Collections.nCopies(expectedResult, null));

        int result = repository.getClientCount();

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void whenGetClientsThenListOfDbTemplateIsReturned() {
        @SuppressWarnings("unchecked")
        List<Client> expectedResult = mock(List.class);

        when(dbTemplate.getCollection(Client.class)).thenReturn(expectedResult);

        List<Client> clients = repository.getClients();
        assertThat(clients).isSameAs(expectedResult);
        verifyZeroInteractions(expectedResult);
    }
}
