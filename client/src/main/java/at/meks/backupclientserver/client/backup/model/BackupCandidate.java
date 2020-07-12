package at.meks.backupclientserver.client.backup.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class BackupCandidate {

    private final Client client;

    private final Path file;

    private final EventType eventType;

    private final String latestRemoteMd5Hex;

    boolean isBackupNeeded() {
        return Files.isRegularFile(file) &&
                ((eventType == EventType.CREATED || eventType == EventType.DELETED) || isChecksumDifferentOnServer());
    }

    @SneakyThrows
    private boolean isChecksumDifferentOnServer() {
        try(final InputStream fileInputStream = Files.newInputStream(file)) {
            return !DigestUtils.md5Hex(fileInputStream).equals(latestRemoteMd5Hex);
        }
    }

    public Client client() {
        return client;
    }

    public Path file() {
        return file;
    }

}