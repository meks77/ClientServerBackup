package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.ClientService;
import at.meks.backupclientserver.backend.services.file.FileService;
import at.meks.backupclientserver.backend.services.file.FileStatistics;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/api/v1.0/statistics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StatisticWebService {

    @Inject
    private FileService fileService;

    @Inject
    private ClientService clientService;

    @Inject
    private ClientRepository clientRepository;

    @Inject
    private ExceptionHandler exceptionHandler;

    @Path("fileStatistics")
    @GET
    public FileStatistics getFileStatistics() {
        return exceptionHandler.runReportingException(() -> "getFileStatistics",
                fileService::getBackupFileStatistics);
    }

    @Path(value="clients/count")
    @GET
    public int getBackupedCientsCount() {
        return exceptionHandler.runReportingException(() -> "getBackupedCientsCount", () -> clientService.getClientCount());
    }

    @Path(value="clients")
    @GET
    public List<StatisticClient> getBackupedCients() {
        return exceptionHandler.runReportingException(() -> "backupFile",
                () -> clientRepository.getAll().stream().map(StatisticClient::fromClient).collect(Collectors.toList()));
    }

    @Path(value="client/{hostName}")
    @GET
    public FileStatistics getClientDiskUsage(@PathParam("hostName") String hostName) {
        return exceptionHandler.runReportingException(() -> "backupFile", () -> {
            Optional<Client> client = clientRepository.getById(hostName);
            return client.map(client1 -> fileService.getDiskUsage(client1))
                    .orElse(FileStatistics.NOT_ANALYZED);
        });
    }

}
