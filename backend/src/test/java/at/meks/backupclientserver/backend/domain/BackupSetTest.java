package at.meks.backupclientserver.backend.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BackupSetTest {

    private BackupSet backupSet = new BackupSet();

    @Test
    public void testClientBackupSetPath() {
        assertThat(backupSet.getClientBackupSetPath()).isNull();
        String expectedPath = "expectedPath";
        backupSet.setClientBackupSetPath(expectedPath);
        assertThat(backupSet.getClientBackupSetPath()).isEqualTo(expectedPath);
    }


    @Test
    public void testDirectoryNameOnServer() {
        String expectedPath = "expectedPath";
        assertThat(backupSet.getDirectoryNameOnServer()).isNull();
        backupSet.setDirectoryNameOnServer(expectedPath);
        assertThat(backupSet.getDirectoryNameOnServer()).isEqualTo(expectedPath);
    }

}
