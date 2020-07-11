package at.meks.backupclientserver.client.backup.model.adapter.persistence;

import at.meks.backupclientserver.client.backup.model.BackupCandidate;
import at.meks.backupclientserver.client.backup.model.BackupCandidateRepository;
import at.meks.backupclientserver.client.backup.model.Client;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

@Singleton
public class RestBackupCandidateRepo implements BackupCandidateRepository {

    @Inject
    @RestClient
    RemoteBackupService remoteBackupService;

    @SneakyThrows
    @Override
    public void save(BackupCandidate backupCandidate) {
        try (final InputStream fileInputStream = Files.newInputStream(backupCandidate.file())) {
            remoteBackupService.backupFile(encode(backupCandidate.client().id()), encode(getParentDirectory(backupCandidate)),
                    encode(getFileName(backupCandidate)), fileInputStream);
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String getParentDirectory(BackupCandidate backupCandidate) {
        return backupCandidate.file().getParent().toString();
    }

    private String getFileName(BackupCandidate backupCandidate) {
        return backupCandidate.file().getFileName().toString();
    }

    @Override
    public Optional<String> getLatestMd5Hex(Client client, Path file) {
        return ofNullable(
                trimToNull(
                        remoteBackupService.getMd5Hex(encode(client.id()), encode(file.getParent().toString()),
                                encode(file.getFileName().toString()))));
    }
}
