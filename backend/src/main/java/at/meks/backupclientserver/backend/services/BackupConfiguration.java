package at.meks.backupclientserver.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class BackupConfiguration {

    @Value("${application.root.dir}")
    private String applicationRoot;

    String getApplicationRoot() {
        return applicationRoot;
    }

}
