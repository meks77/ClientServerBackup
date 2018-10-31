package at.meks.backupclientserver.backend.webservices;

import at.meks.backupclientserver.backend.services.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class BackupWebService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BackupService backupService;

    @RequestMapping(value="/api/v1.0/backup")
    public void backupFile(@RequestParam MultipartFile file, @RequestParam String relativePath,
            @RequestParam("hostName") String hostName, @RequestParam String backupedPath) {
        logger.info("received file for hostName {} and backedupPath {} and relative path {}. File: {}",
                hostName, backupedPath, relativePath, file);
        backupService.backup(file, hostName, backupedPath, relativePath);
        logger.info("backup completed");
    }

}
