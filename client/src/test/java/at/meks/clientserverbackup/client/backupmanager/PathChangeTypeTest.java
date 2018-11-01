package at.meks.clientserverbackup.client.backupmanager;

import org.junit.Test;

import static at.meks.clientserverbackup.client.backupmanager.PathChangeType.CREATED;
import static at.meks.clientserverbackup.client.backupmanager.PathChangeType.DELETED;
import static at.meks.clientserverbackup.client.backupmanager.PathChangeType.MODIFIED;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.fest.assertions.api.Assertions.assertThat;

public class PathChangeTypeTest {

    @Test
    public void givenEntryCreateWhenFromReturnsCreated() {
        assertThat(PathChangeType.from(ENTRY_CREATE)).isEqualTo(CREATED);
    }

    @Test
    public void givenEntryDeleteWhenFromReturnsDeleted() {
        assertThat(PathChangeType.from(ENTRY_DELETE)).isEqualTo(DELETED);
    }

    @Test
    public void givenEntryModifyWhenFromReturnsModified() {
        assertThat(PathChangeType.from(ENTRY_MODIFY)).isEqualTo(MODIFIED);
    }

    @Test
    public void givenUnknownEntryEventWhenFromReturnsNull() {
        assertThat(PathChangeType.from(OVERFLOW)).isNull();
    }

    @Test
    public void whenValuesReturnsOnlyCreatedDeletedAndModified() {
        assertThat(PathChangeType.values()).containsOnly(CREATED, DELETED, MODIFIED);
    }
}
