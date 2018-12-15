package at.meks.backupclientserver.backend.services.persistence;

import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.BackupConfiguration;
import io.jsondb.JsonDBTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class PersistenceService {

    private JsonDBTemplate jsonDBTemplate;

    @Autowired
    private BackupConfiguration configuration;

    @Autowired
    public void initDb() {
        jsonDBTemplate = new JsonDBTemplate(Paths.get(configuration.getApplicationRoot()).resolve("db").toString(),
                Client.class.getPackage().getName());
    }

    JsonDBTemplate getJsonDBTemplate() {
        return jsonDBTemplate;
    }
}
