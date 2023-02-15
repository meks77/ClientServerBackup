package at.meks.backup.server.persistence.file.version;

import at.meks.backup.server.domain.model.file.BackupTime;
import at.meks.backup.server.domain.model.file.BackupedFile;
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
    public void add(BackupedFile backupedFile, BackupTime backupTime, Path file) {
        try {
            persistVersion(backupedFile, backupTime, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void persistVersion(BackupedFile backupedFile, BackupTime backupTime, Path file) throws IOException {
        BackupedFileEntity entity = BackupedFileEntity.findByFileId(backupedFile.id())
                .orElseThrow(() -> new IllegalStateException("Can't backup version of not backuped file " + backupedFile.id()));
        VersionDbEntity version = new VersionDbEntity();
        version.backupedFileEntity = entity;
        version.id = UUID.randomUUID().toString();
        version.backupTime = backupTime.backupTime();
        version.persist();

        FileContent content = new FileContent();
        content.id = UUID.randomUUID().toString();
        content.version = version;
        content.content = BlobProxy.generateProxy(Files.newInputStream(file), Files.size(file));
        content.persist();
    }

}
