package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.domain.Client;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static at.meks.clientserverbackup.testutils.DateTestUtils.fromLocalDateTime;
import static java.time.LocalDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class StatisticClientTest {

    @Test(expected = NullPointerException.class)
    public void givenNullWhenFromThenThrowsNullPointerException() {
        //noinspection ConstantConditions
        StatisticClient.fromClient(null);
    }

    @Test
    public void givenClientWhenFromThenHostNameIsSet() {
        String expectedHostName = "utTestHostName";
        Client client = Client.aClient().name(expectedHostName).build();

        StatisticClient statisticClient = StatisticClient.fromClient(client);

        assertThat(statisticClient.getHostName()).isEqualTo(expectedHostName);
    }

    @Test
    public void givenClientWhenFromThenLastBackupTimestampIsSet() {
        Date expectedBackupTimestamp = fromLocalDateTime(of(2009, 9, 14, 1, 4, 2));
        Client client = Client.aClient().lastBackupedFileTimestamp(expectedBackupTimestamp).build();

        StatisticClient statisticClient = StatisticClient.fromClient(client);

        assertThat(statisticClient.getLastBackupTimestamp()).isEqualTo(expectedBackupTimestamp);
    }

    @Test
    public void givenClientWhenFromThenHeartbeatTimestampIsSet() {
        Date expectedBackupTimestamp = fromLocalDateTime(of(2009, 9, 14, 1, 4, 2));
        Client client = Client.aClient().heartbeatTimestamp(expectedBackupTimestamp).build();

        StatisticClient statisticClient = StatisticClient.fromClient(client);

        assertThat(statisticClient.getHeartbeatTimestamp()).isEqualTo(expectedBackupTimestamp);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void givenClientWhenFromLeavesClientUnchanged() {
        Client client = Mockito.mock(Client.class);
        StatisticClient.fromClient(client);
        verify(client).getHeartbeatTimestamp();
        verify(client).getLastBackupedFileTimestamp();
        verify(client).getName();
        verifyNoMoreInteractions(client);
    }
}