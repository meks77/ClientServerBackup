package at.meks.backupclientserver.backend.webservices.backup;

import at.meks.backupclientserver.backend.domain.ErrorLog;
import at.meks.backupclientserver.backend.services.ClientService;
import at.meks.backupclientserver.backend.services.ErrorReportService;
import at.meks.backupclientserver.common.service.health.ErrorReport;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;


@Path("/api/v1.0/health")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HealthWebService {

    @Inject
    private ClientService clientService;

    @Inject
    private ErrorReportService errorReportService;

    @Inject
    private ExceptionHandler exceptionHandler;

    @Path("heartbeat/{hostName}")
    @PUT
    public void heartbeat(@PathParam("hostName") String hostName) {
        exceptionHandler.runReportingException(() -> "heartbeat", () -> clientService.updateHeartbeat(hostName));
    }

    @Path("error/{hostName}")
    @PUT
    public void addError(@PathParam("hostName") String hostName, ErrorReport errorReport) {
        errorReportService.addError(hostName, errorReport.getMessage(), errorReport.getException());
    }

    @Path("errors/maxSize/{listMaxSize}")
    @GET
    public List<ErrorLog> getErrors(@PathParam("listMaxSize") Integer listMaxSize) {
        return exceptionHandler.runReportingException(() -> "getErrors",
                () -> errorReportService.getErrors(Optional.ofNullable(listMaxSize).orElse(20)));
    }

}
