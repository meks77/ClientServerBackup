package at.meks.backup.server.persistence.client;

import at.meks.backup.server.domain.model.client.Client;
import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.client.ClientName;
import at.meks.backup.server.domain.model.client.ClientRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
class JpaClientRepository implements ClientRepository {

    @Override
    public Optional<Client> get(ClientId id) {
        return ClientEntity.<ClientEntity>findByIdOptional(id.text())
                .map(this::toDomainEntity);
    }

    private Client toDomainEntity(ClientEntity clientEntity) {
        return Client.existingClient(
                ClientId.existingId(clientEntity.id),
                new ClientName(clientEntity.name));
    }

    @Override
    public void add(Client client) {
        ClientEntity entity = new ClientEntity();
        entity.id = client.id().text();
        entity.name = client.name().text();
        entity.persistAndFlush();
    }

    @Override
    public List<Client> findAll() {
        return ClientEntity.<ClientEntity>findAll()
                .stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }
}
