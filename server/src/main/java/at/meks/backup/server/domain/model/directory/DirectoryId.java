package at.meks.backup.server.domain.model.directory;

import static at.meks.validation.args.ArgValidator.validate;

public record DirectoryId(String text) {
    public DirectoryId {
        validate()
                .that(text)
                .withMessage(() -> "id text")
                .isNotBlank();
    }
}
