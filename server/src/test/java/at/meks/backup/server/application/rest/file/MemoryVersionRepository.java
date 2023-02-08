package at.meks.backup.server.application.rest.file;

import at.meks.backup.server.domain.model.file.BackupTime;
import at.meks.backup.server.domain.model.file.FileId;
import at.meks.backup.server.domain.model.file.version.Content;
import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import io.quarkus.test.Mock;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Stream;

@Mock
public class MemoryVersionRepository implements VersionRepository {

    private final Path testRootDir;

    private static final Collection<Version> versions = new LinkedList<>();

    @SneakyThrows
    public MemoryVersionRepository() {
        this.testRootDir = Files.createTempDirectory("fileversions");
    }

    @SneakyThrows
    @Override
    public Version add(FileId fileId, BackupTime backupTime, Path file) {
        Path targetFile = testRootDir.resolve(Path.of(
                fileId.clientId().text(),
                UUID.randomUUID().toString()));
        Files.createDirectories(targetFile.getParent());
        Files.copy(Files.newInputStream(file), targetFile);
        Version version = Version.newVersion(fileId, backupTime, new Content(targetFile.toUri()));
        versions.add(version);
        return version;
    }

    public void clear() {
        versions.clear();
    }

    public Stream<Version> stream() {
        return versions.stream();
    }
}
