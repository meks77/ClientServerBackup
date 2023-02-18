package at.meks.backup.client.infrastructure.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

//TODO tests
@StaticInitSafe
public class CustomConfig implements ConfigSource {

    private static final Properties properties = new Properties();

    static {
        Path properties = Path.of(System.getProperty("user.home"), ".clientServerBackup", "application.properties");
        try {
            Files.createDirectories(properties.getParent());
            if (Files.notExists(properties)) {
                Files.createFile(properties);
            }
            CustomConfig.properties.load(Files.newBufferedReader(properties));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getOrdinal() {
        return 275;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Override
    public String getValue(final String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public String getName() {
        return "Clients-Application-Properties";
    }
}
