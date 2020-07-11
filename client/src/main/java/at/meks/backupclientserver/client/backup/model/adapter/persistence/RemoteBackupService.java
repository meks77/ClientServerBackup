package at.meks.backupclientserver.client.backup.model.adapter.persistence;

import at.meks.backupclientserver.common.service.fileup2date.FileUp2dateResult;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/api/v1.0/{clientId}/{directory}/{filename}")
@RegisterRestClient(configKey = "remote")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public interface RemoteBackupService {

    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    Response backupFile(@PathParam("clientId") String clientId, @PathParam("directory") String encodedDirectory,
                               @PathParam("filename") String encodedFilename, InputStream fileContent);

    @GET
    @Path("/isFileUpToDate")
    FileUp2dateResult isFileUp2date(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                                           @PathParam("filename") String filename, @HeaderParam("md5Checksum") String md5Checksum);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/md5Hex")
    String getMd5Hex(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                            @PathParam("filename") String filename);

    @DELETE
    void deletePath(@PathParam("clientId") String clientId, @PathParam("directory") String directory,
                           @PathParam("filename") String filename);
}
