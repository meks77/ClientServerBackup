package at.meks.backupclientserver.client;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.inject.Inject;

@QuarkusMain
public class BackupClientApplication implements QuarkusApplication {

    @Inject
    ApplicationConfig config;

    @Override
    public int run(String... args) throws Exception {
        config.validate();
        Quarkus.waitForExit();
        return 0;
    }

}
