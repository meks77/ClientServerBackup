package at.meks.backupclientserver.client.backupmanager;

import org.junit.jupiter.api.Test;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.assertj.core.api.Assertions.assertThat;

public class PathChangeTypeTest {

    @Test
    public void givenEntryCreateWhenFromReturnsCreated() {
        assertThat(PathChangeType.from(ENTRY_CREATE)).isEqualTo(PathChangeType.CREATED);
    }

    @Test
    public void givenEntryDeleteWhenFromReturnsDeleted() {
        assertThat(PathChangeType.from(ENTRY_DELETE)).isEqualTo(PathChangeType.DELETED);
    }

    @Test
    public void givenEntryModifyWhenFromReturnsModified() {
        assertThat(PathChangeType.from(ENTRY_MODIFY)).isEqualTo(PathChangeType.MODIFIED);
    }

    @Test
    public void givenUnknownEntryEventWhenFromReturnsNull() {
        assertThat(PathChangeType.from(OVERFLOW)).isNull();
    }

    @Test
    public void whenValuesReturnsOnlyCreatedDeletedAndModified() {
        assertThat(PathChangeType.values()).containsOnly(PathChangeType.CREATED, PathChangeType.DELETED, PathChangeType.MODIFIED);
    }
}
