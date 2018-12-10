package at.meks.clientserverbackup.testutils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTestUtils {

    private DateTestUtils() {}

    public static Date fromLocalDateTime(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
