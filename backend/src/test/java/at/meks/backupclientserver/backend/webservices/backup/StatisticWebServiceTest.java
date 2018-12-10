package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.ClientBuilder;
import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.ClientService;
import at.meks.backupclientserver.backend.services.FileService;
import at.meks.backupclientserver.backend.services.FileStatistics;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.List;

import static at.meks.clientserverbackup.testutils.DateTestUtils.fromLocalDateTime;
import static at.meks.backupclientserver.backend.webservices.backup.StatisticClient.fromClient;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatisticWebServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private FileService fileService;

    @Mock
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private StatisticWebService service = new StatisticWebService();

    @Test
    public void testGetFileStatistics() {
        FileStatistics fileStatistics = mock(FileStatistics.class);
        when(fileService.getBackupFileStatistics()).thenReturn(fileStatistics);
        FileStatistics result = service.getFileStatistics();
        assertThat(result).isSameAs(fileStatistics);
    }

    @Test
    public void testGetBackupedClientsCount() {
        int expectedResult = 16;
        when(clientService.getClientCount()).thenReturn(expectedResult);

        int result = service.getBackupedCientsCount();

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void testGetBackupedClients() {
        Client client1 = ClientBuilder.aClient().
                withName("client1").withLastBackupedFileTimestamp(fromLocalDateTime(now().minusHours(1))).build();
        Client client2 = ClientBuilder.aClient().
                withName("client2").withLastBackupedFileTimestamp(fromLocalDateTime(now().minusHours(2))).build();
        Client client3 = ClientBuilder.aClient().
                withName("client3").withLastBackupedFileTimestamp(fromLocalDateTime(now().minusHours(3))).build();
        List<StatisticClient> expectedResult = asList(fromClient(client1), fromClient(client2), fromClient(client3));

        when(clientRepository.getClients()).thenReturn(Arrays.asList(client1, client2, client3));

        List<StatisticClient> result = service.getBackupedCients();

        assertThat(result).isEqualTo(expectedResult);
    }

}
