package at.meks.backup.server.domain.model.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.concurrent.locks.ReentrantLock;

@Named
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository repository;

    private static final ReentrantLock lock = new ReentrantLock();

    public Client register(ClientName clientName) {
        Client client = Client.newClient(clientName.text());
        repository.add(client);
        return client;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registerIfNotExists(ClientId clientId) {
        lock.lock();
        try {
            if (repository.get(clientId).isEmpty()) {
                repository.add(Client.existingClient(
                        clientId,
                        new ClientName("Autocreated Client for id " + clientId.text())));
            }
        } finally {
            lock.unlock();
        }
    }
}
