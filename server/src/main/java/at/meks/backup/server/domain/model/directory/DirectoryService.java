package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectoryService {

    private final DirectoryRepository repository;

    public void directoryWasAdded(ClientId clientId, PathOnClient path) {
        repository.save(Directory.directoryWasAdded(clientId, path));
    }

    public void directoryWasRemoved(DirectoryId id) {
        repository.get(id)
                .ifPresent(this::removeDirectory);
    }

    private void removeDirectory(Directory directory) {
        directory.directoryWasRemoved();
        repository.save(directory);
    }

}
