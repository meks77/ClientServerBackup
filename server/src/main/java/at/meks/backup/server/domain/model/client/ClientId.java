package at.meks.backup.server.domain.model.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.UUID;

import static at.meks.validation.args.ArgValidator.validate;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
public class ClientId {

    String text;

    public static ClientId existingId(String text) {
        validate()
                .that(text)
                .withMessage(() -> "id text")
                .isNotBlank();
        return new ClientId(text);
    }

    public static ClientId newId() {
        return new ClientId(UUID.randomUUID().toString());
    }

}
