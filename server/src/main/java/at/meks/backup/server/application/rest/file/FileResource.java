package at.meks.backup.server.application.rest.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.BackupedFileService;
import at.meks.backup.server.domain.model.file.Checksum;
import at.meks.backup.server.domain.model.file.FileId;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Path("/v1/clients/{clientId}/file/{filepath}")
public class FileResource {

    @Inject
    BackupedFileService fileService;

    @Path("latestChecksum/{checksum}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Uni<BackupNecessary> isBackupNeeded(
            @PathParam("clientId") String clientId,
            @PathParam("filepath") String filePath,
            @PathParam("checksum") long checksum) {
        log.trace("Status for file {} requested", filePath);
        FileId fileId = FileId.idFor(
                ClientId.existingId(clientId),
                new PathOnClient(Paths.get(filePath)));
        return Uni.createFrom()
                .item(() -> fileService.isBackupNecessarry(fileId, new Checksum(checksum)))
                .onItem().transform(BackupNecessary::new);
    }

    @POST
    @Transactional
    public void backupFile(
            @PathParam("clientId") String clientId,
            @PathParam("filepath") String filePath,
            @RestForm("file") FileUpload uploadedFile) {
        log.trace("Beginn upload of file {}", filePath);
        fileService.backup(
                FileId.idFor(ClientId.existingId(clientId), new PathOnClient(Paths.get(filePath))),
                uploadedFile.uploadedFile());
        FileResource.delete(uploadedFile);
        log.info("Finished upload of file {}", filePath);
    }

    private static void delete(FileUpload path) {
        try {
            Files.delete(path.uploadedFile());
        } catch (IOException e) {
            log.warn("Couldn't delete uploaded file after backup: " + path.uploadedFile(), e);
        }
    }
}