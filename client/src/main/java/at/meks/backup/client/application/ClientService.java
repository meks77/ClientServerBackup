package at.meks.backup.client.application;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/v1/clients")
@RegisterRestClient(configKey="client-api")
public interface ClientService {

    @POST
    @Path("{name}")
    @Transactional
    Uni<Client> add(@PathParam("name") String name);

}
