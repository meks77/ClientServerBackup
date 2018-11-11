package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.BackupService;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1.0/backup")
public class BackupWebService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BackupService backupService;

    @PostMapping(value="/file")
    public void backupFile(@RequestParam MultipartFile file, @RequestParam String[] relativePath,
            @RequestParam("hostName") String hostName, @RequestParam String backupedPath) {
        logger.info("received file for hostName {} and backedupPath {} and relative path {}. File: {}",
                hostName, backupedPath, relativePath, file);
        backupService.backup(file, hostName, backupedPath, relativePath);
        logger.info("backup completed");
    }

    @PostMapping(value = "/isFileUpToDate",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FileUp2dateResult isFileUp2date(@RequestBody FileUp2dateInput fileUp2DateInput) {
        FileUp2dateResult result = new FileUp2dateResult();
        result.setUp2date(backupService.isFileUpToDate(fileUp2DateInput.getHostName(),
                fileUp2DateInput.getBackupedPath(), fileUp2DateInput.getRelativePath(), fileUp2DateInput.getFileName(),
                fileUp2DateInput.getMd5Checksum()));
        return result;
    }

}
