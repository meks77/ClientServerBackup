package at.meks.backup.server.domain.model.client;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Client {

    @EqualsAndHashCode.Include
    ClientId id;
    ClientName name;

    public static Client newClient(String name) {
        return new Client(ClientId.newId(), new ClientName(name));
    }

}
