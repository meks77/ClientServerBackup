package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.common.service.fileup2date.FileInputArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

@Service
public class BackupService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss.SSS");

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private DirectoryService directoryService;

    @Inject
    private MetaDataService metaDataService;

    @Inject
    private ClientService clientService;

    public void backup(MultipartFile file, FileInputArgs fileArgs) {
        runHandlingException("error while backup", () -> {
            File target = getTargetFile(fileArgs);
            logger.info("copy file to target {}", target.getAbsolutePath());
            moveOldFileToVersionsDir(target.toPath(), false);
            file.transferTo(target);
            metaDataService.writeMd5Checksum(target);
            clientService.updateLastBackupTimestamp(fileArgs.getHostName());
            return Void.TYPE;
        });
    }

    private File getTargetFile(FileInputArgs fileArgs)
            throws IOException {
        return getTargetFile(fileArgs, true);
    }

    private File getTargetFile(FileInputArgs fileArgs, boolean createNotExisting) throws IOException {
        Path backupSetPath = directoryService.getBackupSetPath(fileArgs.getHostName(), fileArgs.getBackupedPath());
        Path targetDir = Paths.get(backupSetPath.toString(), fileArgs.getRelativePath());
        if (createNotExisting && !targetDir.toFile().exists()) {
            Files.createDirectories(targetDir);
        }
        return new File(targetDir.toFile(), fileArgs.getFileName());
    }

    private void moveOldFileToVersionsDir(Path outDatedFile, boolean markAsDeleted) throws IOException {
        if (outDatedFile.toFile().exists()) {
            Path targetFile = getVersionTarget(outDatedFile, markAsDeleted);
            Files.move(outDatedFile, targetFile);
        }
    }

    private Path getVersionTarget(Path outDatedFile, boolean markAsDeleted) {
        StringBuilder fileNameInVersionsDir = new StringBuilder(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
        if (markAsDeleted) {
            fileNameInVersionsDir.append("-deleted");
        }
        Path versionsDirectory = directoryService.getFileVersionsDirectory(outDatedFile);
        return versionsDirectory.resolve(fileNameInVersionsDir.toString());
    }

    public boolean isFileUpToDate(FileInputArgs fileArgs, String md5Checksum) {
        return runHandlingException("error while verifying if file is up2date", () -> {
            File backupedFile = getTargetFile(fileArgs);
            return metaDataService.isMd5Equal(backupedFile, md5Checksum);
        });
    }

    public void delete(FileInputArgs fileArgs) {
        runHandlingException("error while deleting path", () -> {
            File target = getTargetFile(fileArgs, false);
            if (target.exists()) {
                if (target.isDirectory()) {
                    Path targetDir = directoryService.getDirectoryForDeletedDir(target.toPath());
                    Files.move(target.toPath(), targetDir);
                } else {
                    moveOldFileToVersionsDir(target.toPath(), true);
                }
            }
            return Void.TYPE;
        });
    }

    private <T> T runHandlingException(String errorMessage, Callable<T> runnable) {
        try {
            return runnable.call();
        } catch (Exception e) {
            logger.error(errorMessage, e);
            throw new ServerBackupException(errorMessage, e);
        }
    }
}
