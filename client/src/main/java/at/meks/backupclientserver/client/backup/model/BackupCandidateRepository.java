package at.meks.backupclientserver.client.backup.model;

import java.nio.file.Path;
import java.util.Optional;

public interface BackupCandidateRepository {

    void save(BackupCandidate backupCandidate);

    Optional<String> getLatestMd5Hex(Client client, Path file);
}
