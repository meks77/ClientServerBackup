package at.meks.backup.server.persistence;

import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionRepository;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class DummyVersionRepository implements VersionRepository {
    @Override
    public void add(Version version) {

    }
}
