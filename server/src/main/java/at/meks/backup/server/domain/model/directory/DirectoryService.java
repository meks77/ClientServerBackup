package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DirectoryService {

    private final DirectoryRepository repository;

    DirectoryService(DirectoryRepository repository) {
        this.repository = repository;
    }

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
