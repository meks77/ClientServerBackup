package at.meks.backup.server.domain.model.time;

import javax.enterprise.context.ApplicationScoped;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@ApplicationScoped
public class UtcClock {

    public ZonedDateTime now() {
        return ZonedDateTime.now(ZoneId.of("Z"));
    }

}
