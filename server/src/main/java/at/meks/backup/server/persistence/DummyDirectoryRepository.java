package at.meks.backup.server.persistence;

import at.meks.backup.server.domain.model.directory.Directory;
import at.meks.backup.server.domain.model.directory.DirectoryId;
import at.meks.backup.server.domain.model.directory.DirectoryRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class DummyDirectoryRepository implements DirectoryRepository {

    @Override
    public void add(Directory addedDirectory) {

    }

    @Override
    public Optional<Directory> get(DirectoryId id) {
        return Optional.empty();
    }
}
