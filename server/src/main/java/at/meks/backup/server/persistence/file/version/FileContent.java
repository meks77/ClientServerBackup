package at.meks.backup.server.persistence.file.version;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Blob;

@Entity
@Table(name = "BACKUPED_FILE_VERSION_CONTENT")
public class FileContent extends PanacheEntityBase {

    @Id
    public String id;

    @OneToOne
    @JoinColumn(name = "VERSION_ID")
    public VersionDbEntity version;

    @Lob
    public Blob content;

}
