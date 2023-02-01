package at.meks.backup.server.domain.model.time;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class UtcClock {

    public ZonedDateTime now() {
        return ZonedDateTime.now(ZoneId.of("Z"));
    }

}
