package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClientRepository extends AbstractRepository<Client, String> {

    private ReentrantLock createNewClientLock = new ReentrantLock();

    @Autowired
    private LockService lockService;

    @Override
    Class<Client> getEntityClass() {
        return Client.class;
    }

    public Client createNewClient(String hostName, String directoryName) {
        return lockService.runWithLock(createNewClientLock,
                () -> createNewClientWithinLock(hostName, directoryName));
    }

    private Client createNewClientWithinLock(String hostName, String directoryName) {
        Client newClient = getJsonDBTemplate().findById(hostName, Client.class);
        if (newClient == null) {
            newClient = Client.aClient().build();
            newClient.setName(hostName);
            newClient.setDirectoryName(directoryName);
            newClient.setBackupSets(new LinkedList<>());
            insert(newClient);
        }
        return newClient;
    }

}
