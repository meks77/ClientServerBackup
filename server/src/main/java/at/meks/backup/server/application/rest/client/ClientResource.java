package at.meks.backup.server.application.rest.client;

import at.meks.backup.server.domain.model.client.ClientName;
import at.meks.backup.server.domain.model.client.ClientService;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/v1/clients")
public class ClientResource {

    @Inject
    ClientService service;

    @POST
    @Path("{name}")
    @Transactional
    public Uni<Client> add(@PathParam("name") String name) {
        return Uni.createFrom()
                .item(new ClientName(name))
                .map(service::register)
                .map(Client::new);
    }

}