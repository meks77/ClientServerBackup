package at.meks.backupclientserver.context.backup.adapter.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/v1.0/heartbeat/{client}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientReportingResource {

    @PUT
    public void setAlive(@PathParam("client") String client) {

    }
}
