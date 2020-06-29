package at.meks.backupclientserver.context.backup.adapter.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "BACKUPED_FILE_DELETIONS")
public class DeletionDate extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "backupedFileId")
    public BackupedFileEntity backupedFile;

    public ZonedDateTime deletionTime;

}
