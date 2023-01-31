package at.meks.backup.server.domain.model.client;

import lombok.NonNull;
import lombok.Value;

@Value
public class Client {

    @NonNull
    ClientId id;
    @NonNull
    ClientName name;
}
