package at.meks.backup.server.domain.model.client;

import static at.meks.validation.args.ArgValidator.validate;

public record ClientId(String text) {

    public ClientId {
        validate()
                .that(text)
                .withMessage(() -> "id text")
                .isNotBlank();
    }

}
