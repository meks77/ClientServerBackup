package at.meks.backup.server.domain.model.client;

import java.util.Optional;

public interface ClientRepository {

    Optional<Client> get(ClientId id);

    void add(Client client);

}
