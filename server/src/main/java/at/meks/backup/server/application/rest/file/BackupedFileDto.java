package at.meks.backup.server.application.rest.file;

import at.meks.backup.server.domain.model.file.BackupedFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BackupedFileDto {

    public static BackupedFileDto from(BackupedFile backupedFile) {
        return BackupedFileDto.builder()
                .path(backupedFile.id().pathOnClient().path())
                .build();
    }

    Path path;

}
