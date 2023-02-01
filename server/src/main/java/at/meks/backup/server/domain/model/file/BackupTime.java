package at.meks.backup.server.domain.model.file;

import java.time.ZonedDateTime;

import static at.meks.validation.args.ArgValidator.validate;

public record BackupTime(ZonedDateTime backupTime) {

    public BackupTime {
        validate().that(backupTime).withMessage(() -> "backupTime").isNotNull();
    }

}
