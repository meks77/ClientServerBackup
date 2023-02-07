package at.meks.backup.server.application.rest;

import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import io.quarkus.test.Mock;

@Mock
public class MemoryVersionRepository implements VersionRepository {

    @Override
    public void add(Version version) {

    }

}
