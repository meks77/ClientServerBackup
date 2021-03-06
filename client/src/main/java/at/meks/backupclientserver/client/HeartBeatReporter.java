package at.meks.backupclientserver.client;

import at.meks.backupclientserver.client.http.HttpUrlResolver;
import at.meks.backupclientserver.client.http.JsonHttpClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
class HeartBeatReporter {

    @Inject
    private HttpUrlResolver urlResolver;

    @Inject
    private JsonHttpClient jsonHttpClient;

    @Inject
    private SystemService systemService;

    @Inject
    private ErrorReporter errorReporter;

    @Inject
    private ServerStatusService serverStatusService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /* for faster testing purpose this field is defined to be able to set it from the test. */
    @SuppressWarnings("FieldCanBeLocal")
    private int interval = 30;

    void startHeartbeatReporting() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::reportHeartbeat, 0, interval, TimeUnit.SECONDS);
    }

    private void reportHeartbeat() {
        try {
            logger.debug("start sending heartbeat");
            String heartbeatUrl = urlResolver.getWebserviceUrl("health", "heartbeat/" + systemService.getHostname());
            jsonHttpClient.put(heartbeatUrl, null, Void.TYPE, false);
            serverStatusService.setServerAvailable(true);
        } catch (Exception e) {
            errorReporter.reportError("couldn't send heartbeat", e);
        }
    }
}
