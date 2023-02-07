package at.meks.backup.server.application.rest;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.DirectoryService;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.nio.file.Paths;

@Path("/v1/clients/")
public class DirectoryResource {

    @Inject
    DirectoryService service;

    @POST
    @Path("{clientId}/{path}")
    public Uni<Directory> add(
            @PathParam("clientId") String clientId,
            @PathParam("path") String directoryPath) {
        return Uni.createFrom()
                .item(new PathOnClient(Paths.get(directoryPath)))
                .map(path -> service.directoryWasAdded(ClientId.existingId(clientId), path))
                .map(Directory::new);
    }

}
