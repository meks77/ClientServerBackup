package at.meks.backup.client.infrastructure.remote;

import at.meks.backup.client.model.Checksum;
import at.meks.backup.client.model.ClientId;
import at.meks.backup.client.model.FileService;
import io.smallrye.mutiny.Uni;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

@ApplicationScoped
@Slf4j
public class QuarkusFileService implements FileService {

    @RestClient
    RemoteFileService remoteFileService;

    @Override
    public void fireBackupIfNecessary(ClientId clientId, Path file, Checksum checksum, Consumer<Path> fileConsumer) {
        log.trace("get backup status from server for {}", file);
        Uni.createFrom()
                .item(() -> remoteFileService.isBackupNeeded(clientId.text(), file.toString(), checksum.hash()))
                .onFailure().invoke(throwable -> log.error("remote call failed", throwable))
                .onItem()
                .transform(BackupNecessary::isBackupNecessary)
                .subscribe()
                .with(backupNecessary -> fireFileChangedEvent(file, fileConsumer, backupNecessary));
    }

    private static void fireFileChangedEvent(Path file, Consumer<Path> fileConsumer, Boolean backupNecessary) {
        log.trace("Answer for {} from remote: {}", file, backupNecessary);
        if (backupNecessary)
            fileConsumer.accept(file);
    }

    @SneakyThrows
    @Override
    public void backup(ClientId clientId, Path file) {
        remoteFileService.backup(clientId.text(), file.toString(), new MultipartBody(new FileInputStream(file.toFile())));
        log.trace("File uploaded for backup: {}", file);
    }
}
