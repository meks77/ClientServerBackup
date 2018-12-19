package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/api/v1.0/health")
public class HealthWebService {

    @Autowired
    private ClientService clientService;

    @PutMapping(value = "heartbeat/{hostName}")
    public void heartbeat(@PathVariable String hostName) {
        clientService.updateHeartbeat(hostName);
    }
}
