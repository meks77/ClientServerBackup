package at.meks.backup.client.infrastructure.remote;

import at.meks.backup.client.model.ClientId;
import at.meks.backup.client.model.FileService;
import at.meks.backup.shared.model.Checksum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileInputStream;
import java.nio.file.Path;

@ApplicationScoped
@Slf4j
public class QuarkusFileService implements FileService {

    @RestClient
    RemoteFileService remoteFileService;

    @SneakyThrows
    @Override
    public void backup(ClientId clientId, Path file) {
        remoteFileService.backup(clientId.text(), file.toString(), new MultipartBody(new FileInputStream(file.toFile())));
        log.trace("File uploaded for backup: {}", file);
    }

    @Override
    public boolean isBackupNecessary(ClientId clientId, Path file, Checksum checksum) {
        return remoteFileService.isBackupNeeded(clientId.text(), file.toString(), checksum.hash()).backupNecessary;
    }
}
