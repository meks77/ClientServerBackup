package at.meks.backup.client.infrastructure.context.quarkus;

import at.meks.backup.client.application.ExitAction;
import io.quarkus.runtime.Quarkus;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QuarkusExit implements ExitAction {
    @Override
    public void exit() {
        Quarkus.asyncExit(0);
    }
}
