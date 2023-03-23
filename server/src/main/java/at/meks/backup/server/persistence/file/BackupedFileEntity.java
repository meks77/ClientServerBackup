package at.meks.backup.server.persistence.file;

import at.meks.backup.server.domain.model.file.FileId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Optional;

@Entity
@Table(name = "BACKUPED_FILE")
@ToString
@NamedQuery(name = "BackupedFileEntity.findByFileId",
        query = "from BackupedFileEntity where clientId = :clientId and pathOnClient = :pathOnClient")
public class BackupedFileEntity extends PanacheEntityBase {

    @Id
    public String id;

    @Column(name = "CLIENT_ID")
    public String clientId;

    @Column(name = "PATH_ON_CLIENT")
    public String pathOnClient;

    @Column(name = "LATEST_VERSION_CHECKSUM")
    public Long latestVersionChecksum;

    @Column(name = "LATEST_SIZE")
    public Long latestSize;

    public static Optional<BackupedFileEntity> findByFileId(FileId fileId) {
        return BackupedFileEntity.<BackupedFileEntity>find(
                        "#BackupedFileEntity.findByFileId",
                        Parameters.with("clientId", fileId.clientId().text())
                                .and("pathOnClient", fileId.pathOnClient().asText()).map())
                .firstResultOptional();
    }
}
