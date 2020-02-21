package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.services.file.UploadService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Path("/file")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FileUploadService {

    @Inject
    UploadService uploadService;

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public String upload(InputStream inputStream) {
        return uploadService.addNewFile(inputStream).toString();
    }

}
