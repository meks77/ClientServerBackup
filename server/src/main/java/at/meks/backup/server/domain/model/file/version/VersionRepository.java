package at.meks.backup.server.domain.model.file.version;

import java.nio.file.Path;

public interface VersionRepository {

    void add(Version version, Path content);
}
