package at.meks.backup.server.persistence;

import at.meks.backup.server.domain.model.file.BackupTime;
import at.meks.backup.server.domain.model.file.FileId;
import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionRepository;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;


@ApplicationScoped
public class DummyVersionRepository implements VersionRepository {

    @Override
    public Version add(FileId fileId, BackupTime backupTime, Path file) {
        return null;
    }
}
