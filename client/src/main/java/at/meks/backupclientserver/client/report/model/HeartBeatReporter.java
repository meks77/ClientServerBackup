package at.meks.backupclientserver.client.report.model;

import at.meks.backupclientserver.client.ErrorReporter;
import at.meks.backupclientserver.client.ServerStatusService;
import io.quarkus.scheduler.Scheduled;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
class HeartBeatReporter {

    @Inject
    ErrorReporter errorReporter;

//    @Inject
//    ServerStatusService serverStatusService;

    @Scheduled(every = "30s")
    void reportHeartbeat() {
        try {
            log.info("start sending heartbeat");
            // TODO use remote service class to to heartbeat, if necessary
//            jsonHttpClient.put(heartbeatUrl, null, Void.TYPE, false);
            // TODO move server available to backup or different responsibility!?
//            serverStatusService.setServerAvailable(true);
        } catch (Exception e) {
            errorReporter.reportError("couldn't send heartbeat", e);
        }
    }
}
