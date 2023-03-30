package at.meks.backup.server.application.html;

import at.meks.backup.server.domain.model.file.BackupedFile;
import at.meks.backup.shared.model.Checksum;

public record File(String name, String latestChecksum, String sizeInMb) {

    static File fileFor(BackupedFile backupedFile) {
        return new File(
                backupedFile.id().pathOnClient().path().toString(),
                backupedFile.latestVersionChecksum().map(Checksum::hash).map(String::valueOf).orElse(""),
                String.format("%.3f", backupedFile.latestSize() / 1024d / 1024d));
    }

}
