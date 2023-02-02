package at.meks.backup.server.domain.model.file.version;

import at.meks.backup.server.domain.model.file.FileHash;

import java.net.URI;

public record Content(URI uriToContent) {
    public FileHash hash() {
        //TODO implement has des Contents
        return new FileHash(0);
    }
}
