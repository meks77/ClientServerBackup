package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static at.meks.validation.args.ArgValidator.validate;

@Accessors(fluent = true, chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class Directory {

    @EqualsAndHashCode.Include
    private final DirectoryId id;

    private final ClientId clientId;
    private final PathOnClient path;

    @Setter(AccessLevel.PRIVATE)
    private boolean active;

    public static Directory directoryWasAdded(ClientId clientId, PathOnClient path) {
        return new Directory(clientId, path)
                .active(true);
    }

    private Directory(ClientId clientId, PathOnClient path) {
        validate().that(clientId).withMessage(() -> "clientId").isNotNull();
        validate().that(path).withMessage(() -> "path").isNotNull();

        this.clientId = clientId;
        this.path = path;
        id = DirectoryId.idFor(clientId, path);
    }

    public void directoryWasRemoved(){
        active = false;
    }

}
