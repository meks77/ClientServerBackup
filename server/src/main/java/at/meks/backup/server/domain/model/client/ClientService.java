package at.meks.backup.server.domain.model.client;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;

    public void register(ClientId clientId, ClientName clientName) throws ClientAlreadyRegistered {
        Optional<Client> client = repository.find(clientId);
        if (client.isPresent()) {
            throw new ClientAlreadyRegistered(clientId);
        }
        repository.create(new Client(clientId, clientName));
    }
}
