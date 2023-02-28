package at.meks.backup.client.infrastructure.remote;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("/v1/clients/{clientId}/file/{filepath}")
@RegisterRestClient(configKey = "remote-api")
public interface RemoteFileService {


    @Path("latestChecksum/{checksum}")
    @GET
    BackupNecessary isBackupNeeded(
            @PathParam("clientId") String clientId,
            @PathParam("filepath") String filePath,
            @PathParam("checksum") long checksum);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    void backup(
            @PathParam("clientId") String clientId,
            @PathParam("filepath") String filePath,
            @MultipartForm MultipartBody uploadedFile);
}
