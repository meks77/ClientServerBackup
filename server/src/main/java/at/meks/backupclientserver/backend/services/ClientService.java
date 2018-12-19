package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public int getClientCount() {
        return clientRepository.getClientCount();
    }

    void updateLastBackupTimestamp(String hostName) {
        Optional<Client> clientOptional = clientRepository.getClient(hostName);
        clientOptional.ifPresent(client1 -> {
            client1.setLastBackupedFileTimestamp(new Date());
            clientRepository.update(client1);
        });
    }

    public void updateHeartbeat(String hostName) {
        Optional<Client> clientOptional = clientRepository.getClient(hostName);
        if (clientOptional.isPresent()) {
            Client client = clientOptional.get();
            client.setHeartbeatTimestamp(new Date());
            clientRepository.update(client);
        }
    }
}
