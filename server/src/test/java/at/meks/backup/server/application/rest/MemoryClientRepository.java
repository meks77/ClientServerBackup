package at.meks.backup.server.application.rest;

import at.meks.backup.server.domain.model.client.Client;
import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.client.ClientRepository;
import io.quarkus.test.Mock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Mock
public class MemoryClientRepository implements ClientRepository {

    private final Collection<Client> clients = new HashSet<>();

    @Override
    public Optional<Client> get(ClientId id) {
        return clients.stream()
                .filter(client -> client.id().equals(id))
                .findFirst();
    }

    @Override
    public void add(Client client) {
        clients.add(client);
    }
}
