package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
@RequiredArgsConstructor
public class DirectoryService {

    private final DirectoryRepository repository;

    public Directory directoryWasAdded(ClientId clientId, PathOnClient path) {
        Directory addedDirectory = Directory.directoryWasAdded(clientId, path);
        repository.add(addedDirectory);
        return addedDirectory;
    }

    public void directoryWasRemoved(DirectoryId id) {
        repository.get(id)
                .ifPresent(this::removeDirectory);
    }

    private void removeDirectory(Directory directory) {
        directory.directoryWasRemoved();
        repository.add(directory);
    }

}
