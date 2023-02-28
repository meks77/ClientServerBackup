package at.meks.backup.server.application.html;

import at.meks.backup.server.domain.model.client.ClientRepository;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static java.util.Objects.requireNonNull;

@Path("/")
public class SomePage {

    private final Template page;
    private final Template clients;
    private final ClientRepository clientRepository;

    public SomePage(Template page, Template clients, ClientRepository clientRepository) {
        this.page = requireNonNull(page, "page is required");
        this.clients = clients;
        this.clientRepository = clientRepository;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        return page.data("name", name);
    }

    @GET
    @Path("/clients")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance clients() {
        return clients.data("clients", clientRepository.findAll());
    }





}
