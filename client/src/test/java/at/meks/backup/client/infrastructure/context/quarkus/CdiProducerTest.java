package at.meks.backup.client.infrastructure.context.quarkus;

import at.meks.backup.client.application.ExitAction;
import at.meks.backup.client.application.Start;
import at.meks.backup.client.infrastructure.config.QuarkusConfig;
import at.meks.backup.client.infrastructure.events.QuarkusEventBus;
import at.meks.backup.client.model.Config;
import at.meks.backup.client.model.Events;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class CdiProducerTest {

    @Inject
    Start applicationStart;

    @Inject
    Config config;

    @Inject
    Events events;

    @Inject
    ExitAction exitAction;

    @Test
    void startClassIAvailable() {
        assertThat(applicationStart).isNotNull();
    }

    @Test
    void configClassIsAvailable() {
        assertThat(config).isInstanceOf(QuarkusConfig.class);
    }

    @Test
    void eventsClassIsAvailable() {
        assertThat(events).isInstanceOf(QuarkusEventBus.class);
    }

    @Test
    void exitActionIsAvailable() {
        assertThat(exitAction).isInstanceOf(QuarkusExit.class);
    }
}