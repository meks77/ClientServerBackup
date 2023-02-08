package at.meks.backup.server.domain.model.directory;

import java.nio.file.Path;

import static at.meks.validation.args.ArgValidator.validate;

public record PathOnClient(Path path) {

    public PathOnClient {
        validate()
                .that(path)
                .withMessage(() -> "path")
                .isNotNull();
    }

    public String asText() {
        return path.toString().replace("\\", "/");
    }
}
