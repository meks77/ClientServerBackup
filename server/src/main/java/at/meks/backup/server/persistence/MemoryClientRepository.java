package at.meks.backup.server.persistence;

import at.meks.backup.server.domain.model.client.Client;
import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.client.ClientRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Named
@ApplicationScoped
public class MemoryClientRepository implements ClientRepository {

    private final Collection<Client> clients = new HashSet<>();

    @Override
    public Optional<Client> find(ClientId id) {
        return clients.stream()
                .filter(client -> client.id().equals(id))
                .findFirst();
    }

    @Override
    public void create(Client client) {
        clients.add(client);
    }
}
