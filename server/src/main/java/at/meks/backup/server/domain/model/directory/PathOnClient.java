package at.meks.backup.server.domain.model.directory;

import java.nio.file.Path;

public class PathOnClient {
    private final Path path;

    public PathOnClient(Path path) {
        this.path = path;
    }

    public String asText() {
        return path.toString();
    }
}
