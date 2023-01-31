package at.meks.backup.server.domain.model.client;

import static java.lang.String.format;

public class ClientAlreadyRegistered extends Exception {
    public ClientAlreadyRegistered(ClientId clientId) {
        super(format(
                "Client with id %s is already registered. Please choose a different id for registration",
                clientId.text()));
    }
}
