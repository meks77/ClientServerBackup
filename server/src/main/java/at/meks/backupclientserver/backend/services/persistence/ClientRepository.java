package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.LockService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

@Named
@ApplicationScoped
public class ClientRepository extends AbstractRepository<Client, String> {

    private ReentrantLock createNewClientLock = new ReentrantLock();

    @Inject
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
