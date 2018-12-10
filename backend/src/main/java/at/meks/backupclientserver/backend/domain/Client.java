package at.meks.backupclientserver.backend.domain;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Date;

@Document(collection = "clients", schemaVersion = "1.0")
@Getter @Setter
public class Client {

    @Id
    private String name;

    private String directoryName;

    private Collection<BackupSet> backupSets;

    private Date lastBackupedFileTimestamp;

}