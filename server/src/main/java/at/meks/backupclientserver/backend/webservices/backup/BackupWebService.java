package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.backup.BackupService;
import at.meks.backupclientserver.backend.services.file.UploadService;
import at.meks.backupclientserver.common.service.BackupCommandArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateInput;
import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/v1.0/backup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BackupWebService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private BackupService backupService;

    @Inject
    private UploadService uploadService;

    @Inject
    private ExceptionHandler exceptionHandler;

    @POST
    public void backupFile(BackupCommandArgs backupCommandArgs) {
        exceptionHandler.runReportingException(() -> "backupFile", () -> {
            logger.info("received file for hostName {} and backedupPath {} and relative path {}. File: {}",
                    backupCommandArgs.getHostName(), backupCommandArgs.getBackupedPath(),
                    backupCommandArgs.getRelativePath(), backupCommandArgs.getRelativePathUplodadedFile());
            FileInputArgs fileInputArgs =
                    FileInputArgs.aFileInputArgs().hostName(backupCommandArgs.getHostName()).backupedPath(backupCommandArgs.getBackupedPath())
                            .relativePath(backupCommandArgs.getRelativePath()).fileName(backupCommandArgs.getFileName()).build();
            backupService.backup(uploadService.getAbsolutePath(backupCommandArgs.getRelativePathUplodadedFile()), fileInputArgs);
            logger.info("backup completed");
        });
    }

    @Path("/isFileUpToDate")
    @GET
    public FileUp2dateResult isFileUp2date(FileUp2dateInput fileUp2DateInput) {
        return exceptionHandler.runReportingException(() -> "isFileUp2date", () -> {
            boolean upToDate = backupService.isFileUpToDate(fileUp2DateInput, fileUp2DateInput.getMd5Checksum());
            return new FileUp2dateResult(upToDate);
        });
    }

    @DELETE
    public void deletePath(FileInputArgs deletePathArgs) {
        exceptionHandler.runReportingException(() -> "deletePath", () -> backupService.delete(deletePathArgs));

    }

}
