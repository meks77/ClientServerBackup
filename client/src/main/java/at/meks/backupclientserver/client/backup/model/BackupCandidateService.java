package at.meks.backupclientserver.client.backup.model;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import io.vertx.mutiny.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class BackupCandidateService {

    public static final int BACKUP_DELAY = 500;

    @Inject
    BackupCandidateRepository repository;

    @Inject
    EventBus eventBus;

    @Inject
    Vertx vertx;

    @SneakyThrows
    @ConsumeEvent(value = "backup")
    public void fileRegisteredForBackup(FileChangedEvent fileChangedEvent) {
        final Path changedFile = fileChangedEvent.changedFile();
        if (Files.getLastModifiedTime(changedFile).toInstant().plusMillis(BACKUP_DELAY).isAfter(ZonedDateTime.now().toInstant())) {
            log.info("Delay backup because file modification date is too new");
            vertx.setTimer(BACKUP_DELAY, id -> eventBus.publish("backup", fileChangedEvent));
        } else {
            log.info("do backup");
            final Client client = fileChangedEvent.client();
            final EventType eventType = fileChangedEvent.eventType();
            log.info("before getting md5");
            try {
                final Optional<String> latestRemoteMd5Hex = repository.getLatestMd5Hex(client, changedFile);

                log.info("md5 of remote: {}", latestRemoteMd5Hex);
                BackupCandidate backupCandidate = new BackupCandidate(client, changedFile, eventType, latestRemoteMd5Hex.orElse(null));
                if (backupCandidate.isBackupNeeded()) {
                    log.info("backup is necessary");
                    repository.save(backupCandidate);
                } else {
                    log.info("backup is not necessary");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
