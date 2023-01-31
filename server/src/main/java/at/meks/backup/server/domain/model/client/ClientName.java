package at.meks.backup.server.domain.model.client;

import static at.meks.validation.args.ArgValidator.validate;

public record ClientName(String text) {

    public ClientName {
        validate().that(text)
                .withMessage(() -> "client name text")
                .isNotNull()
                .isNotBlank();
    }

}
