package at.meks.backup.server.application.html;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.file.BackupedFileRepository;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/files/{clientId}")
public class ClientFiles {

    private final Template clientFiles;
    private final BackupedFileRepository fileRepository;

    public ClientFiles(Template clientFiles, BackupedFileRepository fileRepository) {
        this.clientFiles = clientFiles;
        this.fileRepository = fileRepository;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance files(@PathParam("clientId") String clientId) {
        return clientFiles.data("files", fileRepository.find(ClientId.existingId(clientId)));
    }
}
