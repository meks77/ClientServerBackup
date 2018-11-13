package at.meks.backupclientserver.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BackupService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss.SSS");

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private MetaDataService metaDataService;

    public void backup(MultipartFile file, String hostName, String backupedPath, String[] relativePath, String fileName) {
        File target = getTargetFile(fileName, hostName, backupedPath, relativePath);
        try {
            logger.info("copy file to target {}", target.getAbsolutePath());
            moveOldFileToVersionsDir(target.toPath());
            file.transferTo(target);
            metaDataService.writeMd5Checksum(target);
        } catch (IOException e) {
            throw new ServerBackupException("error while tranfer to target file", e);
        }
    }

    private File getTargetFile(String fileName, String hostName, String backedupDir, String[] relativePathWithinDir) {
        Path backupSetPath = directoryService.getBackupSetPath(hostName, backedupDir);
        Path targetDir = Paths.get(backupSetPath.toString(), relativePathWithinDir);
        if (!targetDir.toFile().exists()) {
            try {
                Files.createDirectories(targetDir);
            } catch (IOException e) {
                throw new ServerBackupException("couldn't create directories " + targetDir, e);
            }
        }
        return new File(targetDir.toFile(), fileName);
    }

    private void moveOldFileToVersionsDir(Path outDatedFile) throws IOException {
        if (outDatedFile.toFile().exists()) {
            String fileNameInVersionsDir = DATE_TIME_FORMATTER.format(LocalDateTime.now());
            Files.move(outDatedFile, directoryService.getFileVersionsDirectory(outDatedFile).resolve(fileNameInVersionsDir));
        }
    }

    public boolean isFileUpToDate(String hostName, String backupedPath, String[] relativePath, String fileName, String md5Checksum) {
        File backupedFile = getTargetFile(fileName, hostName, backupedPath, relativePath);
        return metaDataService.isMd5Equal(backupedFile, md5Checksum);
    }
}
