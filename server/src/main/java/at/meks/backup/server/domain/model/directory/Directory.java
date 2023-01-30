package at.meks.backup.server.domain.model.directory;

import at.meks.backup.server.domain.model.client.ClientId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class Directory {

    @EqualsAndHashCode.Include
    private final ClientId clientId;

    private final PathOnClient path;

    private final DirectoryId id;

    @Setter(AccessLevel.PRIVATE)
    private boolean active;

    public static Directory directoryWasAdded(ClientId clientId, PathOnClient path) {
        return new Directory(clientId, path)
                .active(true);
    }

    private Directory(ClientId clientId, PathOnClient path) {
        this.clientId = clientId;
        this.path = path;
        id = new DirectoryId(clientId.id() + ":" + path.asText());
    }

    public void directoryWasRemoved(){
        active = false;
    }

}
