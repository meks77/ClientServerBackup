package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.LockService;
import io.jsondb.JsonDBTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Optional.ofNullable;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClientRepository {

    private ReentrantLock createNewClientLock = new ReentrantLock();

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private LockService lockService;

    @Autowired
    public void initDb() {
        if (!persistenceService.getJsonDBTemplate().collectionExists(Client.class)) {
            persistenceService.getJsonDBTemplate().createCollection(Client.class);
        }
    }

    private JsonDBTemplate getJsonDBTemplate() {
        return persistenceService.getJsonDBTemplate();
    }

    public Optional<Client> getClient(String hostName) {
        return ofNullable(getJsonDBTemplate().findById(hostName, Client.class));
    }

    public Client createNewClient(String hostName, String directoryName) {
        return lockService.runWithLock(createNewClientLock,
                () -> createNewClientWithinLock(hostName, directoryName));
    }

    private Client createNewClientWithinLock(String hostName, String directoryName) {
        Client newClient = getJsonDBTemplate().findById(hostName, Client.class);
        if (newClient == null) {
            newClient = Client.builder().build();
            newClient.setName(hostName);
            newClient.setDirectoryName(directoryName);
            newClient.setBackupSets(new LinkedList<>());
            getJsonDBTemplate().insert(newClient);
        }
        return newClient;
    }

    public void update(Client client) {
        getJsonDBTemplate().save(client, Client.class);
    }

    public int getClientCount() {
        return getJsonDBTemplate().getCollection(Client.class).size();
    }

    public List<Client> getClients() {
        return getJsonDBTemplate().getCollection(Client.class);
    }
}
