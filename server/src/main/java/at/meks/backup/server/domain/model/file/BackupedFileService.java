package at.meks.backup.server.domain.model.file;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class BackupedFileService {

    private BackupedFileRepository fileRepository;

    public void backup(FileId fileId, byte[] bytes) {
        Optional<BackupedFile> backupedFile = fileRepository.get(fileId);
        if (backupedFile.isEmpty()) {
            fileRepository.save(BackupedFile.newFileForBackup(fileId));
        }
    }
}
