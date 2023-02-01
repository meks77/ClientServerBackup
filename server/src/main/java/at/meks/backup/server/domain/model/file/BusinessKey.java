package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import static at.meks.validation.args.ArgValidator.validate;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BusinessKey {

    ClientId clientId;
    PathOnClient pathOnClient;

    public static BusinessKey idFor(ClientId clientId, PathOnClient path) {
        validate().that(clientId).withMessage(() -> "clientId").isNotNull();
        validate().that(path).withMessage(() -> "path").isNotNull();
        return new BusinessKey(clientId, path);
    }

}
