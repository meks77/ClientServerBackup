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
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Optional;

@javax.ws.rs.Path("/api/v1.0/{clientId}/{directory}/{filename}")
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

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Transactional
    @Counted(name = "performedBackups", description = "How many file backups have been performed.")
    @Timed(name = "backupsTimer", description = "A measure of how long it takes to perform the file backup.", unit = MetricUnits.MILLISECONDS)
    public Response backupFile(@PathParam("clientId") String clientId, @PathParam("directory") String encodedDirectory,
                           @PathParam("filename") String encodedFilename, InputStream fileContent) {
        String filename = decode(encodedFilename);
        String directoryName = decode(encodedDirectory);
        log.info("backup file {}/{} for client {}", directoryName, filename, clientId );
        Optional<BackupedFile> persistedFile = findBackupedFile(clientId, directoryName, filename);
        try {
            if(persistedFile.isEmpty()) {
                backupNewFile(clientId, directoryName, filename, fileContent);
            } else {
                backupNewVersion(fileContent, persistedFile.get());
            }
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage(), e);
            return Response.status(HttpResponseStatus.BAD_REQUEST.code(), e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
                    .entity(e.getMessage()).build();
        } finally {
            log.info("backup completed");
        }
    }

    private String decode(@PathParam("filename") String encodedFilename) {
        return URLDecoder.decode(encodedFilename, StandardCharsets.UTF_8);
    }

    private void backupNewFile(@PathParam("clientId") String clientId, @PathParam("directory") String directory, @PathParam("filename") String filename, InputStream fileContent) {
        BackupedFile backupedFile = BackupedFile.backupNewFile(() -> configuration.uploadDir(), fileSystem,
                new Client(clientId),
                getContainingDirectory(directory),
                filename,
                ZonedDateTime.now(), fileContent);
        repository.save(backupedFile);
    }

    private void backupNewVersion(InputStream fileContent, BackupedFile persistedFile) {
        persistedFile.updateBackupedFile(() -> configuration.uploadDir(), fileSystem, ZonedDateTime.now(), fileContent);
        repository.update(persistedFile);
        log.info("backup new version completed");
    }

    private Path toPath(String encodedPath) {
        return Path.of(encodedPath);
    }

    private Directory getContainingDirectory(String directory) {
        return new Directory(toPath(directory));
    }

    @GET
    @javax.ws.rs.Path("/isFileUpToDate")
    @Counted(name = "performedIsFileUp2date", description = "How many file status checks have been performed.")
    @Timed(name = "isFileUp2dateTimer", description = "A measure of how long it takes to perform a file status check.", unit = MetricUnits.MILLISECONDS)
    public FileUp2dateResult isFileUp2date(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                                           @PathParam("filename") String filename, @HeaderParam("md5Checksum") String md5Checksum) {
        Optional<BackupedFile> backupedFile = findBackupedFile(clientId, decode(directory), decode(filename));
        boolean upToDate = backupedFile.map(file -> file.isCurrentChecksumEqualTo(md5Checksum)).orElse(false);
        log.info("{}}/{}} is up2date: {}", directory, filename, upToDate);
        return new FileUp2dateResult(upToDate);
    }

    @GET
    @javax.ws.rs.Path("/md5Hex")
    @Counted(name = "performedMd5Hex", description = "How many file md5hex have been requested")
    @Timed(name = "md5Hex", description = "A measure how long it takes to get the md5Hex from a file", unit = MetricUnits.MILLISECONDS)
    @Produces(MediaType.TEXT_PLAIN)
    public String getMd5Hex(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                             @PathParam("filename") String filename) {
        Optional<BackupedFile> backupedFile = findBackupedFile(clientId, decode(directory), decode(filename));
        return backupedFile.map(BackupedFile::latestMd5Hex).orElse("");
    }

    private Optional<BackupedFile> findBackupedFile(String clientId, String directory, String filename) {
        return repository.findById(
                getBackupedFileId(clientId, directory, filename));
    }

    private String getBackupedFileId(String clientId, String directory, String filename) {
        return BackupedFile.getIdFor(new Client(clientId), getContainingDirectory(directory), filename);
    }

    @DELETE
    @Transactional
    public void deletePath(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                           @PathParam("filename") String filename) {
        log.info("delete file {}/{} of client {}", directory, filename, clientId);
        Optional<BackupedFile> backupedFile = findBackupedFile(clientId, directory, filename);
        if (backupedFile.isEmpty()) {
            return;
        }
        backupedFile.get().markDeleted(ZonedDateTime.now());
        repository.update(backupedFile.get());
        log.info("delete completed");
    }

}
