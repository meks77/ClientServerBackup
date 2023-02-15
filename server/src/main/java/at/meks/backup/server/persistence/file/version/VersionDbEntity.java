package at.meks.backup.server.persistence.file.version;

import at.meks.backup.server.persistence.file.BackupedFileEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "BACKUPED_FILE_VERSION")
@ToString
public class VersionDbEntity extends PanacheEntityBase {

    @Id
    public String id;

    @ManyToOne
    @JoinColumn(name = "BACKUPED_FILE_ID")
    public BackupedFileEntity backupedFileEntity;

    @Column(name = "BACKUP_TIME")
    public ZonedDateTime backupTime;

}
