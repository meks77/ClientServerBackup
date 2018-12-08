package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClientService {

    @Inject
    private ClientRepository clientRepository;

    public int getClientCount() {
        return clientRepository.getClientCount();
    }
}
