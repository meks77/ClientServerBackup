package at.meks.backupclientserver.backend.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BackupSet {

    private String clientBackupSetPath;

    private String directoryNameOnServer;

}
