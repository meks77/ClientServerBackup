package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Optional;

@Named
@ApplicationScoped
public class ClientService {

    @Inject
    private ClientRepository clientRepository;

    public int getClientCount() {
        return clientRepository.getSize();
    }

    public void updateLastBackupTimestamp(String hostName) {
        Optional<Client> clientOptional = clientRepository.getById(hostName);
        clientOptional.ifPresent(client1 -> {
            client1.setLastBackupedFileTimestamp(new Date());
            clientRepository.update(client1);
        });
    }

    public void updateHeartbeat(String hostName) {
        Optional<Client> clientOptional = clientRepository.getById(hostName);
        if (clientOptional.isPresent()) {
            Client client = clientOptional.get();
            client.setHeartbeatTimestamp(new Date());
            clientRepository.update(client);
        }
    }
}
