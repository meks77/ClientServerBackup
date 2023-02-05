package at.meks.backup.server.application.rest;

import lombok.Value;

@Value
public class Client {

    String id;
    String name;

    public Client(at.meks.backup.server.domain.model.client.Client newClient) {
        id = newClient.id().text();
        name = newClient.name().text();
    }

}
