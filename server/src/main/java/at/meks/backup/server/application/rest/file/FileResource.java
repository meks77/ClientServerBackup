package at.meks.backup.server.application.rest.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.BackupedFileService;
import at.meks.backup.server.domain.model.file.FileId;
import at.meks.backup.shared.model.Checksum;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
        String decodedFilePath = decode(filePath);
        log.trace("Status for file {} requested", decodedFilePath);
        FileId fileId = FileId.idFor(
                ClientId.existingId(clientId),
                new PathOnClient(Paths.get(decodedFilePath)));
        return Uni.createFrom()
                .item(() -> fileService.isBackupNecessarry(fileId, new Checksum(checksum)))
                .onItem().transform(BackupNecessary::new);
    }

    private String decode(String filePath) {
        return URLDecoder.decode(filePath, StandardCharsets.UTF_8);
    }

    @POST
    @Transactional
    public void backupFile(
            @PathParam("clientId") String clientId,
            @PathParam("filepath") String filePath,
            @RestForm("file") FileUpload uploadedFile) {
        String decodedFilePath = decode(filePath);
        log.trace("Beginn upload of file {}", decodedFilePath);
        fileService.backup(
                FileId.idFor(ClientId.existingId(clientId), new PathOnClient(Paths.get(decodedFilePath))),
                uploadedFile.uploadedFile());
        FileResource.delete(uploadedFile);
        log.info("Finished upload of file {}", decodedFilePath);
    }

    private static void delete(FileUpload path) {
        try {
            Files.delete(path.uploadedFile());
        } catch (IOException e) {
            log.warn("Couldn't delete uploaded file after backup: " + path.uploadedFile(), e);
        }
    }
}