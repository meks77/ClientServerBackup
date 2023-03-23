package at.meks.backup.server.domain.model.file.version;

import java.util.UUID;

public record VersionId(String uuid) {

    public static VersionId newId() {
        return new VersionId(UUID.randomUUID().toString());
    }
}
