package at.meks.backup.server.application.rest;

import lombok.Value;

@Value
public class Directory {

    String id;
    String path;

    public Directory(at.meks.backup.server.domain.model.directory.Directory directory) {
        id = directory.id().text();
        path = directory.path().asText();
    }

}
