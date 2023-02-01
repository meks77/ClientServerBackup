package at.meks.backup.server.domain.model.client;

import static at.meks.validation.args.ArgValidator.validate;

public record Client (ClientId id, ClientName name) {

    public Client {
        validate().that(id).withMessage(() -> "id").isNotNull();
        validate().that(name).withMessage(() -> "name").isNotNull();
    }

}
