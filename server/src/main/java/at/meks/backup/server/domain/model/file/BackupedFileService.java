package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.time.UtcClock;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class BackupedFileService {

    private BackupedFileRepository fileRepository;
    private VersionRepository versionRepository;
    private UtcClock clock;

    public void backup(BusinessKey businessKey, Content fileContent) {
        Optional<BackupedFile> backupedFile = fileRepository.get(businessKey);
        if (backupedFile.isEmpty()) {
            BackupedFile newFile = fileRepository.add(BackupedFile.newFileForBackup(businessKey));
            versionRepository.add(newFile.newVersion(new BackupTime(clock.now()), fileContent));
        }
    }
}
