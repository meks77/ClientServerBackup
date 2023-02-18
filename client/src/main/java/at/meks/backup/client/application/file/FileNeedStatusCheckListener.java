package at.meks.backup.client.application.file;

import io.micrometer.core.annotation.Counted;
import io.quarkus.vertx.ConsumeEvent;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Slf4j
public class FileNeedStatusCheckListener {

    @ConsumeEvent(Events.STATUS_CHECK_QUEUE)
    @Counted
    void fileNeedsStatusCheck(FileNeedsStatusCheckEvent event) {
        log.info("start backup {}", event.file());
    }

}
