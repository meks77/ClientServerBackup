package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.domain.Client;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter @EqualsAndHashCode
class StatisticClient {

    String hostName;

    Date lastBackupTimestamp;

    static StatisticClient fromClient(Client client) {
        StatisticClient statisticClient = new StatisticClient();
        statisticClient.hostName = client.getName();
        statisticClient.lastBackupTimestamp = client.getLastBackupedFileTimestamp();
        return statisticClient;
    }

}
