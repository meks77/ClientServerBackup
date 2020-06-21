package at.meks.backupclientserver.context.infrastructure;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;

@ConfigProperties(prefix = "application")
public interface Configuration {

    @ConfigProperty(name = "root.dir")
    Path rootDir();

    @ConfigProperty(name = "upload.dir")
    Path uploadDir();

}
