package at.meks.backup.client.infrastructure.remote;

import at.meks.backup.client.model.ClientId;
import at.meks.backup.client.model.FileService;
import at.meks.backup.shared.model.Checksum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@ApplicationScoped
@Slf4j
public class QuarkusFileService implements FileService {

    @RestClient
    RemoteFileService remoteFileService;

    @SneakyThrows
    @Override
    public void backup(ClientId clientId, Path file) {
        remoteFileService.backup(
                clientId.text(),
                encode(file),
                new MultipartBody(new FileInputStream(file.toFile())));
        log.trace("File uploaded for backup: {}", file);
    }

    private static String encode(Path file) {
        return URLEncoder.encode(file.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public boolean isBackupNecessary(ClientId clientId, Path file, Checksum checksum) {
        return remoteFileService.isBackupNeeded(
                clientId.text(),
                encode(file),
                checksum.hash())
                .backupNecessary;
    }
}
