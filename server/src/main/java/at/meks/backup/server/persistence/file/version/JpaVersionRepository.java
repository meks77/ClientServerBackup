package at.meks.backup.server.persistence.file.version;

import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.persistence.file.BackupedFileEntity;
import org.hibernate.engine.jdbc.BlobProxy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Named
@ApplicationScoped
public class JpaVersionRepository implements VersionRepository {

    @Override
    public void add(Version version, Path content) {
        try {
            persistVersion(version, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void persistVersion(Version version, Path file) throws IOException {
        BackupedFileEntity entity = BackupedFileEntity.findByFileId(version.fileId())
                .orElseThrow(() -> new IllegalStateException("Can't backup version of not backuped file " + version.fileId()));
        VersionDbEntity versionDb = new VersionDbEntity();
        versionDb.backupedFileEntity = entity;
        versionDb.id = version.id().uuid();
        versionDb.backupTime = version.backupTime().backupTime();
        versionDb.size = version.size();
        versionDb.persist();

        FileContent content = new FileContent();
        content.id = UUID.randomUUID().toString();
        content.version = versionDb;
        content.content = BlobProxy.generateProxy(Files.newInputStream(file), Files.size(file));
        content.persist();
    }

}
