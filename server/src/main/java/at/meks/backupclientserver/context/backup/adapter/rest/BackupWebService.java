package at.meks.backupclientserver.context.backup.adapter.rest;

import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import at.meks.backupclientserver.context.backup.model.BackupedFile;
import at.meks.backupclientserver.context.backup.model.BackupedFileRepository;
import at.meks.backupclientserver.context.backup.model.Client;
import at.meks.backupclientserver.context.backup.model.Directory;
import at.meks.backupclientserver.context.backup.model.FileSystem;
import at.meks.backupclientserver.context.infrastructure.Configuration;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Base64;

@javax.ws.rs.Path("/api/v1.0/backup/{clientId}/{directory}/{filename}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class BackupWebService {

    @Inject
    BackupedFileRepository repository;

    @Inject
    FileSystem fileSystem;

    @Inject
    Configuration configuration;

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void backupFile(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                           @PathParam("filename") String filename, InputStream fileContent) {
        log.info("backup file {}/{} for client {}", directory, filename, clientId );
        BackupedFile backupedFile = BackupedFile.backupNewFile(() -> configuration.uploadDir(), fileSystem,
                new Client(clientId),
                getContainingDirectory(directory),
                filename,
                ZonedDateTime.now(), fileContent);
        repository.save(backupedFile);
        log.info("backup completed");
    }

    private Path toPath(String encodedPath) {
        return Path.of(encodedPath);
    }

    private Directory getContainingDirectory(String directory) {
        return new Directory(toPath(directory));
    }

    @GET
    @javax.ws.rs.Path("isFileUpToDate")
    public FileUp2dateResult isFileUp2date(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                                           @PathParam("filename") String filename, @HeaderParam("md5Checksum") String md5Checksum) {
        BackupedFile backupedFile = findBackupedFile(clientId, directory, filename);
        boolean upToDate = backupedFile.isCurrentChecksumEqualTo(md5Checksum);
        return new FileUp2dateResult(upToDate);
    }

    private BackupedFile findBackupedFile(String clientId, String directory, String filename) {
        return repository.findById(
                getBackupedFileId(clientId, directory, filename));
    }

    private String getBackupedFileId(String clientId, String directory, String filename) {
        return BackupedFile.getIdFor(new Client(clientId), getContainingDirectory(directory), filename);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response backupChangedFile(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                                  @PathParam("filename") String filename, InputStream fileContent) {
        log.info("backup new version of file {}/{} of client {}", directory, filename, clientId);
        BackupedFile backupedFile = findBackupedFile(clientId, directory, filename);
        try {
            backupedFile.updateBackupedFile(() -> configuration.uploadDir(), fileSystem, ZonedDateTime.now(), fileContent);
            repository.update(backupedFile);
            log.info("backup new version completed");
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(HttpResponseStatus.BAD_REQUEST.code(), e.getMessage()).build();
        }
    }

    @DELETE
    public void deletePath(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                           @PathParam("filename") String filename) {
        log.info("delete file {}/{} of client {}", directory, filename, clientId);
        BackupedFile backupedFile = findBackupedFile(clientId, directory, filename);
        backupedFile.markDeleted(ZonedDateTime.now());
        repository.update(backupedFile);
        log.info("delete completed");
    }

}
