package at.meks.backupclientserver.backend.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
class MetaDataService {

    @Autowired
    private DirectoryService directoryService;

    void writeMd5Checksum(File target) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(target)) {
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            Files.write(getMd5FilePath(target), md5Hex.getBytes());
        }
    }

    private Path getMd5FilePath(File backupedFile) {
        Path metadataDirectoryPath = directoryService.getMetadataDirectoryPath(backupedFile.getParentFile().toPath());
        return Paths.get(metadataDirectoryPath.toString(), backupedFile.getName() + ".md5");
    }

    boolean isMd5Equal(File backupedFile, String md5Checksum) {
        Path md5FilePath = getMd5FilePath(backupedFile);
        if (!md5FilePath.toFile().exists()) {
            return false;
        }
        try {
            String backupedMd5 = Files.readAllLines(md5FilePath).get(0);
            return backupedMd5.equals(md5Checksum);
        } catch (IOException e) {
            throw new ServerBackupException("couldn't read md5 checksum", e);
        }
    }
}
