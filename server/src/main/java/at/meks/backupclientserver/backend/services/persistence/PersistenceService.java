package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.BackupConfiguration;
import io.jsondb.JsonDBTemplate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Paths;

@Named
@ApplicationScoped
class PersistenceService {

    private JsonDBTemplate jsonDBTemplate;

    @Inject
    private BackupConfiguration configuration;

    @Inject
    public void initDb() {
        jsonDBTemplate = new JsonDBTemplate(Paths.get(configuration.getApplicationRoot()).resolve("db").toString(),
                Client.class.getPackage().getName());
    }

    JsonDBTemplate getJsonDBTemplate() {
        return jsonDBTemplate;
    }
}
