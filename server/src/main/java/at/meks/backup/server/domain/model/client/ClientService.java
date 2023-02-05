package at.meks.backup.server.domain.model.client;

import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;

    public Client register(ClientName clientName) {
        Client client = Client.newClient(clientName.text());
        repository.create(client);
        return client;
    }
}
