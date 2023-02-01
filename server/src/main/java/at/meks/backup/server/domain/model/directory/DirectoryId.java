package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import static at.meks.validation.args.ArgValidator.validate;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = false)
public class DirectoryId {

    String text;

    public static DirectoryId idFor(ClientId clientId, PathOnClient path) {
        validate().that(clientId).withMessage(() -> "clientId").isNotNull();
        validate().that(path).withMessage(() -> "path").isNotNull();
        return new DirectoryId(String.format("%s:%s", clientId.text(), path.asText()));
    }

}
