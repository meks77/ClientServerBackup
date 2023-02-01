package at.meks.backup.server.domain.model.file;


import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

import static at.meks.validation.args.ArgValidator.validate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BackupedFile {

    @EqualsAndHashCode.Include
    @Getter
    private final BusinessKey id;

    static BackupedFile newFileForBackup(BusinessKey id) {
        validate().that(id).withMessage(() -> "id").isNotNull();
        return new BackupedFile(id);
    }

    public Version newVersion(BackupTime backupTime, Content fileContent) {
        return new Version(new VersionId(UUID.randomUUID()), id, backupTime, fileContent);
    }

}
