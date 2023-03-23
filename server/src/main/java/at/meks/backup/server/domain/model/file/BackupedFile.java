package at.meks.backup.server.domain.model.file;


import at.meks.backup.shared.model.Checksum;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Optional;

import static at.meks.validation.args.ArgValidator.validate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true, chain = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BackupedFile {

    @EqualsAndHashCode.Include
    @Getter
    private final FileId id;

    private Checksum latestVersionChecksum;

    @Setter
    @Getter
    private long latestSize;

    public static BackupedFile newFileForBackup(FileId id) {
        validate().that(id).withMessage(() -> "id").isNotNull();
        return new BackupedFile(id);
    }

    public Optional<Checksum> latestVersionChecksum() {
        return Optional.ofNullable(latestVersionChecksum);
    }

    public void versionWasBackedup(Checksum checksum) {
        this.latestVersionChecksum = checksum;
    }
}
