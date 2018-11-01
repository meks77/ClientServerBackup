package at.meks.clientserverbackup.client.backupmanager;

import org.junit.Test;

import java.nio.file.Path;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TodoEntryTest {

    @Test
    public void givenChangeTypeCreatedThenGetTypeReturnsCreated() {
        PathChangeType changeType = PathChangeType.CREATED;
        TodoEntry todoEntry = new TodoEntry(changeType, mock(Path.class), mock(Path.class));
        assertThat(todoEntry.getType()).isSameAs(changeType);
    }

    @Test
    public void givenChangeTypeDeletedThenGetTypeReturnsDeleted() {
        PathChangeType changeType = PathChangeType.DELETED;
        TodoEntry todoEntry = new TodoEntry(changeType, mock(Path.class), mock(Path.class));
        assertThat(todoEntry.getType()).isSameAs(changeType);
    }

    @Test
    public void givenChangedFileWhenConstructingIsReturnedByGetChangedFile() {
        Path changedFile = mock(Path.class);
        TodoEntry todoEntry = new TodoEntry(PathChangeType.CREATED, changedFile, mock(Path.class));
        assertThat(todoEntry.getChangedFile()).isSameAs(changedFile);
    }

    @Test
    public void givenWatchedPathWhenConstructinIsReturnedByGetWatchedPath() {
        Path watchedPath = mock(Path.class);
        TodoEntry todoEntry = new TodoEntry(PathChangeType.CREATED, mock(Path.class), watchedPath);
        assertThat(todoEntry.getWatchedPath()).isSameAs(watchedPath);
    }

    @Test
    public void whenToStringReturnsNotNull() {
        assertThat(new TodoEntry(null, null, null).toString()).isNotNull();
    }
}
