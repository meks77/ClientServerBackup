package at.meks.backupclientserver.context.backup.adapter.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "BACKUPED_FILE_VERSION")
public class VersionEntity extends PanacheEntity {

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "backupedFileId")
    public BackupedFileEntity backupedFile;

    public int version;

    public ZonedDateTime timestampOfBackup;

    public String relativePathToContent;

    public String checkSum;

}
