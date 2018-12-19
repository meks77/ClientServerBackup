package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.BackupService;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
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
            @RequestParam("hostName") String hostName, @RequestParam String backupedPath,
            @RequestParam String fileName) {
        logger.info("received file for hostName {} and backedupPath {} and relative path {}. File: {}",
                hostName, backupedPath, relativePath, file);
        FileInputArgs fileInputArgs =
                FileInputArgs.aFileInputArgs().hostName(hostName).backupedPath(backupedPath)
                        .relativePath(relativePath).fileName(fileName).build();
        backupService.backup(file, fileInputArgs);
        logger.info("backup completed");
    }

    @PostMapping(value = "/isFileUpToDate",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FileUp2dateResult isFileUp2date(@RequestBody FileUp2dateInput fileUp2DateInput) {
        boolean upToDate = backupService.isFileUpToDate(fileUp2DateInput, fileUp2DateInput.getMd5Checksum());
        return new FileUp2dateResult(upToDate);
    }

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void deletePath(@RequestBody FileInputArgs deletePathArgs) {
        backupService.delete(deletePathArgs);
    }

}
