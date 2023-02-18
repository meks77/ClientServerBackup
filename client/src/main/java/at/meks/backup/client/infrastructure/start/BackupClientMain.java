package at.meks.backup.client.infrastructure.start;


import at.meks.backup.client.application.Start;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@QuarkusMain
@Slf4j
public class BackupClientMain implements QuarkusApplication {

    @Inject
    Start applicationStart;

    @Override
    public int run(String... args) throws Exception {
        applicationStart.start();
        Quarkus.waitForExit();
        return 0;
    }

}
