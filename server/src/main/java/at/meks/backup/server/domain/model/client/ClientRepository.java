package at.meks.backup.server.domain.model.client;

import java.util.Optional;

public interface ClientRepository {
    Optional<Client> find(ClientId id);

    void create(Client client);
}
