package at.meks.backup.server.application.rest;

import at.meks.backup.server.domain.model.directory.Directory;
import at.meks.backup.server.domain.model.directory.DirectoryId;
import at.meks.backup.server.domain.model.directory.DirectoryRepository;
import io.quarkus.test.Mock;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.Collection;
import java.util.Optional;

@Mock
public class MemoryDirectoryRespository implements DirectoryRepository {

    private static final Collection<Directory> directories = new ConcurrentHashSet<>();

    @Override
    public void add(Directory addedDirectory) {
        directories.add(addedDirectory);
    }

    @Override
    public Optional<Directory> get(DirectoryId id) {
        return directories.stream()
                .filter(directory -> directory.id().equals(id))
                .findFirst();
    }

    public Collection<Directory> list() {
        return directories;
    }

    public void clear() {
        directories.clear();
    }
}
