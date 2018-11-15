package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.common.Md5CheckSumGenerator;
import org.glassfish.jersey.internal.guava.CacheBuilder;
import org.glassfish.jersey.internal.guava.CacheLoader;
import org.glassfish.jersey.internal.guava.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class DirectoryService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss.SSS");

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BackupConfiguration configuration;

    private Md5CheckSumGenerator md5CheckSumGenerator = new Md5CheckSumGenerator();

    private LoadingCache<String, ReentrantLock> clientLocks = CacheBuilder.newBuilder().build(
            new CacheLoader<String, ReentrantLock>() {
                @Override
                public ReentrantLock load(String hostName) {
                    return new ReentrantLock();
                }
            });

    Path getBackupSetPath(String hostName, String clientBackupSetPath) {
        Path clientRootDir = getClientRootDirectory(hostName);
        String backupSetRelativePath = md5CheckSumGenerator.md5HexFor(clientBackupSetPath);
        Path backupSetPath = Paths.get(clientRootDir.toString(), backupSetRelativePath);

        return createIfNotExists(backupSetPath);
    }

    private Path getClientRootDirectory(String hostName) {
        Path path = Paths.get(getBackupRootDirectory().toString(),
                md5CheckSumGenerator.md5HexFor(hostName));
        return createIfNotExists(path);
    }

    private Path createIfNotExists(Path path) {
        ReentrantLock lock = getLock(path);
        lock.lock();
        try {
            if (!path.toFile().exists()) {
                try {
                    logger.info("create directory {}", path);
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new ServerBackupException("couldn't create directory", e);
                }
            }
            return path;
        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock getLock(Path path) {
        try {
            return clientLocks.get(path.toString());
        } catch (ExecutionException e) {
            throw new ServerBackupException("couldn't get lock from cache", e);
        }
    }

    Path getMetadataDirectoryPath(Path targetDir) {
        Path metaDataDir = Paths.get(targetDir.toString(), ".backupClientServer");
        return createIfNotExists(metaDataDir);
    }

    Path getFileVersionsDirectory(Path changedFile) {
        Path versionsDir = getMetadataDirectoryPath(changedFile.getParent()).resolve(changedFile.toFile().getName());
        return createIfNotExists(versionsDir);
    }

    Path getDirectoryForDeletedDir(Path deletedDir) {
        Path versionsDir = getDeletedDirsDirectory(deletedDir).resolve(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
        return createIfNotExists(versionsDir).resolve(deletedDir.toFile().getName());
    }

    private Path getDeletedDirsDirectory(Path deletedDirectory){
        Path deletedDirsDirectory = getMetadataDirectoryPath(deletedDirectory.getParent()).resolve("deletedDirs");
        return createIfNotExists(deletedDirsDirectory);
    }

    private Path getBackupRootDirectory() {
        String applicationRoot = configuration.getApplicationRoot();
        if (applicationRoot == null) {
            throw new ServerBackupException("applicationRoot directory is not set");
        }
        Path backupDir = Paths.get(applicationRoot, "backups");
        return createIfNotExists(backupDir);
    }
}
