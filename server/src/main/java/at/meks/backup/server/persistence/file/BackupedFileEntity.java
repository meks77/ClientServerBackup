package at.meks.backup.server.persistence.file;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

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

}
