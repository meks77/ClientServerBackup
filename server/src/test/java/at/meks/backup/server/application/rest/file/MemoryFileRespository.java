package at.meks.backup.server.application.rest.file;

import at.meks.backup.server.domain.model.file.BackupedFile;
import at.meks.backup.server.domain.model.file.BackupedFileRepository;
import at.meks.backup.server.domain.model.file.FileId;
import io.quarkus.test.Mock;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.Collection;
import java.util.Optional;

@Mock
public class MemoryFileRespository implements BackupedFileRepository {

    private static final Collection<BackupedFile> files = new ConcurrentHashSet<>();

    @Override
    public BackupedFile add(BackupedFile file) {
        files.add(file);
        return file;
    }

    @Override
    public Optional<BackupedFile> get(FileId id) {
        return files.stream()
                .filter(file -> file.id().equals(id))
                .findFirst();
    }

    public Collection<BackupedFile> list() {
        return files;
    }

    public void clear() {
        files.clear();
    }
}
