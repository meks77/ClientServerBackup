package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.ClientService;
import at.meks.backupclientserver.backend.services.ErrorReportService;
import at.meks.backupclientserver.common.service.health.ErrorReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/api/v1.0/health")
public class HealthWebService {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ErrorReportService errorReportService;

    @PutMapping(value = "heartbeat/{hostName}")
    public void heartbeat(@PathVariable String hostName) {
        clientService.updateHeartbeat(hostName);
    }

    @PutMapping(value = "error/{hostName}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addError(@PathVariable String hostName, @RequestBody ErrorReport errorReport) {
        errorReportService.addError(hostName, errorReport.getMessage(), errorReport.getException());
    }
}
