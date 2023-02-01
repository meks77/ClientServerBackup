package at.meks.backup.server.domain.model.file;

import at.meks.backup.server.domain.model.time.UtcClock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BackupedFileService {

    private BackupedFileRepository fileRepository;
    private VersionRepository versionRepository;
    private UtcClock clock;

    public void backup(BusinessKey businessKey, Content fileContent) {
        BackupedFile backupedFile = fileRepository.get(businessKey)
                .orElseGet(() ->fileRepository.add(BackupedFile.newFileForBackup(businessKey)));

        versionRepository.add(backupedFile.newVersion(new BackupTime(clock.now()), fileContent));
    }
}
