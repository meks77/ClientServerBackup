package at.meks.backupclientserver.client.backup.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class Client {

    private final String id;

    public String id() {
        return id;
    }
}
