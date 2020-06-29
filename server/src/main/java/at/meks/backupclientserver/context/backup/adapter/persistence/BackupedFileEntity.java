package at.meks.backupclientserver.context.backup.adapter.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "BACKUPED_FILE")
public class BackupedFileEntity extends PanacheEntityBase {

    @Id
    public String id;

    public String clientId;

    public String containingDirectory;

    public String fileName;

    @OneToMany(mappedBy = "backupedFile", cascade = CascadeType.PERSIST)
    public List<VersionEntity> versions;

    @OneToMany(mappedBy = "backupedFile")
    public List<DeletionDate> deletedTimestamps;

}
