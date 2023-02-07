package at.meks.backup.server.persistence;

import at.meks.backup.server.domain.model.client.Client;
import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.client.ClientRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class DummyClientRepository implements ClientRepository {

    @Override
    public Optional<Client> get(ClientId id) {
        return Optional.empty();
    }

    @Override
    public void add(Client client) {

    }
}
