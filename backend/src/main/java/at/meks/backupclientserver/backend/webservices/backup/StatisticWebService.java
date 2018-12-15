package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.ClientService;
import at.meks.backupclientserver.backend.services.FileService;
import at.meks.backupclientserver.backend.services.FileStatistics;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1.0/statistics")
@CrossOrigin(origins = "*")
public class StatisticWebService {

    @Autowired
    private FileService fileService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping(value="fileStatistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FileStatistics getFileStatistics() {
        return fileService.getBackupFileStatistics();
    }

    @GetMapping(value="clients/count", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public int getBackupedCientsCount() {
        return clientService.getClientCount();
    }

    @GetMapping(value="clients", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<StatisticClient> getBackupedCients() {
        return clientRepository.getClients().stream().map(StatisticClient::fromClient).collect(Collectors.toList());
    }

    @GetMapping(value="client/{hostName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FileStatistics getClientDiskUsage(@PathVariable String hostName) {
        Optional<Client> client = clientRepository.getClient(hostName);
        return client.map(client1 -> fileService.getDiskUsage(client1))
                .orElse(FileStatistics.NOT_ANALYZED);
    }

}
