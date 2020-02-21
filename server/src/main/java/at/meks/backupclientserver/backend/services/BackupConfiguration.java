package at.meks.backupclientserver.backend.services;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class BackupConfiguration {

    @ConfigProperty(name = "application.root.dir")
    private String applicationRoot;

    public String getApplicationRoot() {
        return applicationRoot;
    }

}
