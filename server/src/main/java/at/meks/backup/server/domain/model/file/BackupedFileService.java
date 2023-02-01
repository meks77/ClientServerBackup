package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.file.version.Content;
import at.meks.backup.server.domain.model.file.version.Version;
import at.meks.backup.server.domain.model.file.version.VersionRepository;
import at.meks.backup.server.domain.model.time.UtcClock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BackupedFileService {

    private BackupedFileRepository fileRepository;
    private VersionRepository versionRepository;
    private UtcClock clock;

    public void backup(FileId fileId, Content fileContent) {
        if (fileRepository.get(fileId).isEmpty()) {
                fileRepository.add(BackupedFile.newFileForBackup(fileId));
        }
        versionRepository.add(Version.newVersion(fileId, new BackupTime(clock.now()), fileContent));
    }
}
