package at.meks.backup.server.application.html;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/css/styles.css")
public class Styles {

    @Inject
    @Location("styles.css")
    Template styles;

    @GET
    @Produces("text/css")
    public TemplateInstance styles() {
        return styles.instance();
    }
}
