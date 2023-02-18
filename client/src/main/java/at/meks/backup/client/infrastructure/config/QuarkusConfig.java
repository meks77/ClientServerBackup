package at.meks.backup.client.infrastructure.config;


import at.meks.backup.client.model.ClientId;
import at.meks.backup.client.model.Config;
import at.meks.backup.client.model.DirectoryForBackup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.util.List;

// TODO tests
@ApplicationScoped
public class QuarkusConfig implements Config {

    @ConfigProperty(name = "at.meks.backup.client.id")
    String clientId;

    @ConfigProperty(name = "at.meks.backup.client.directories", defaultValue = " ")
    List<String> backupedDirectories;


    @Override
    public ClientId clientId() {
        return new ClientId(clientId);
    }

    @Override
    public DirectoryForBackup[] backupedDirectories() {
        return backupedDirectories.stream()
                .map(f -> new DirectoryForBackup(Path.of(f)))
                .toArray(DirectoryForBackup[]::new);
    }
}
