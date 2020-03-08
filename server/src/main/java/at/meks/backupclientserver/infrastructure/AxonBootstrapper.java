package at.meks.backupclientserver.infrastructure;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.Configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@ApplicationScoped
class AxonBootstrapper {

    private AtomicBoolean started = new AtomicBoolean();

    @Inject
    Configuration configuration;

    void onStart(@Observes StartupEvent evt) {
        configuration.start();
        started.set(true);
        log.info("Started Axon configuration");
    }

    void onStop(@Observes ShutdownEvent event) {
        if (started.compareAndSet(true, false)) {
            configuration.shutdown();
            log.info("Stopped Axon configuration");
        }
    }

}
