package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.client.ClientId;
import at.meks.backup.server.domain.model.directory.PathOnClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;

import static at.meks.validation.args.ArgValidator.validate;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@ToString
public class  FileId {

    ClientId clientId;
    PathOnClient pathOnClient;

    public static FileId idFor(ClientId clientId, PathOnClient path) {
        validate().that(clientId).withMessage(() -> "clientId").isNotNull();
        validate().that(path).withMessage(() -> "path").isNotNull();
        return new FileId(clientId, path);
    }

}
