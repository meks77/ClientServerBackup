package at.meks.backupclientserver.client.backup.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.nio.file.Path;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class FileChangedEvent {

    private final Client client;

    private final Path changedFile;

    private final EventType eventType;

    public Client client() {
        return client;
    }

    public Path changedFile() {
        return changedFile;
    }

    public EventType eventType() {
        return eventType;
    }
}
