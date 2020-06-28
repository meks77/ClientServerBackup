package at.meks.backupclientserver.client;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
@Slf4j
class HeartBeatReporter {

    @Inject
    ErrorReporter errorReporter;

    @Inject
    ServerStatusService serverStatusService;

    /* for faster testing purpose this field is defined to be able to set it from the test. */
    @SuppressWarnings("FieldCanBeLocal")
    private int interval = 30;

    void startHeartbeatReporting() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::reportHeartbeat, 0, interval, TimeUnit.SECONDS);
    }

    private void reportHeartbeat() {
        try {
            log.debug("start sending heartbeat");
            // TODO use remote service class to to heartbeat, if necessary
//            jsonHttpClient.put(heartbeatUrl, null, Void.TYPE, false);
            serverStatusService.setServerAvailable(true);
        } catch (Exception e) {
            errorReporter.reportError("couldn't send heartbeat", e);
        }
    }
}
