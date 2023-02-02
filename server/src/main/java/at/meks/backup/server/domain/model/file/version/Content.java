package at.meks.backup.server.domain.model.file.version;

import at.meks.backup.server.domain.model.file.Checksum;

import java.net.URI;

public record Content(URI uriToContent) {
    public Checksum hash() {
        //TODO implement has des Contents
        return new Checksum(0);
    }
}
