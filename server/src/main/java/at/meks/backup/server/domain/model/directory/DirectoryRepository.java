package at.meks.backup.server.domain.model.directory;

import java.util.Optional;

public interface DirectoryRepository {
    void save(Directory addedDirectory);

    Optional<Directory> get(DirectoryId id);
}
