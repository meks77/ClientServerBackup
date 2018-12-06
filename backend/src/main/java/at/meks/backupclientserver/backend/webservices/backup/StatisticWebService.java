package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.FileService;
import at.meks.backupclientserver.backend.services.FileStatistics;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@RestController
@RequestMapping(path = "/api/v1.0/statistics")
@CrossOrigin(origins = "http://localhost:4200")
public class StatisticWebService {

    @Inject
    private FileService fileService;

    @GetMapping(value="fileStatistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FileStatistics getFileStatistics() {
        return fileService.getBackupFileStatistics();
    }

}
