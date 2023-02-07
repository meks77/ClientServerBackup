package at.meks.backup.server.application.rest;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import at.meks.backup.server.domain.model.file.BackupedFileService;
import at.meks.backup.server.domain.model.file.Checksum;
import at.meks.backup.server.domain.model.file.FileId;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.nio.file.Paths;

@Path("/v1/clients")
public class FileResource {

    @Inject
    BackupedFileService fileService;

    @Path("{clientId}/{directoryId}/{filepath}/{checksum}")
    public Uni<Boolean> isBackupNeeded(
            @PathParam("clientId") String clientId,
            @PathParam("filepath") String filePath,
            @PathParam("checksum") long checksum) {
        return Uni
                .createFrom()
                .item(() -> FileId.idFor(
                                ClientId.existingId(clientId),
                                new PathOnClient(Paths.get(filePath))))
                .map(fileId -> fileService.isBackupNecessarry(fileId, new Checksum(checksum)));
    }

}