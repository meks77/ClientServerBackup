package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectoryServiceTest {

    static final PathOnClient PICTURES_PATH = new PathOnClient(Path.of("home", "bbanner", "Pictures"));
    static final ClientId CLIENT_ID = ClientId.newId();

    @InjectMocks
    DirectoryService service;

    @Mock
    DirectoryRepository repository;

    @Test void completlyNewDirectoryWasAdded() {
        service.directoryWasAdded(CLIENT_ID, PICTURES_PATH);

        verify(repository)
                .save(Directory.directoryWasAdded(CLIENT_ID, PICTURES_PATH));
    }

    @Test void directoryWasRemoved() {
        Directory directory = Directory.directoryWasAdded(CLIENT_ID, PICTURES_PATH);
        when(repository.get(directory.id()))
                .thenReturn(Optional.of(directory));

        service.directoryWasRemoved(directory.id());

        verify(repository)
                .save(same(directory));
        assertThat(directory.active())
                .isFalse();
    }

    @Test void directoryWasAlreadyRemoved() {
        Directory directory = Directory.directoryWasAdded(CLIENT_ID, PICTURES_PATH);
        directory.directoryWasRemoved();
        when(repository.get(directory.id()))
                .thenReturn(Optional.of(directory));

        service.directoryWasRemoved(directory.id());

        verify(repository)
                .save(same(directory));
        assertThat(directory.active())
                .isFalse();
    }

    @Test void removedDirectoryWasNotAdded() {
        DirectoryId id = DirectoryId.idFor(ClientId.newId(), new PathOnClient(Path.of("whatever")));
        when(repository.get(id))
                .thenReturn(Optional.empty());

        service.directoryWasRemoved(id);

        verify(repository, never()).save(any());
    }


}
