package at.meks.backupclientserver.backend.services;

import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DirectoryService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BackupConfiguration configuration;

    private LoadingCache<String, ReentrantLock> clientLocks = CacheBuilder.newBuilder().build(
            new CacheLoader<String, ReentrantLock>() {
                @Override
                public ReentrantLock load(String hostName) {
                    return new ReentrantLock();
                }
            });

    Path getBackupSetPath(String hostName, String clientBackupSetPath) {
        Path clientRootDir = getClientRootDirectory(hostName);
        String backupSetRelativePath = DigestUtils.md5Hex(clientBackupSetPath);
        Path backupSetPath = Paths.get(clientRootDir.toString(), backupSetRelativePath);

        createIfNotExists(hostName, backupSetPath);
        return backupSetPath;
    }

    private Path getClientRootDirectory(String hostName) {
        Path path = Paths.get(configuration.getApplicationRootDirectory().toString(), DigestUtils.md5Hex(hostName));
        createIfNotExists(hostName, path);
        return path;
    }

    private void createIfNotExists(String hostName, Path path) {
        ReentrantLock lock = getClientLock(hostName);
        lock.lock();
        try {
            if (!path.toFile().exists()) {
                try {
                    logger.info("create directory {}", path);
                    Files.createDirectory(path);
                } catch (IOException e) {
                    throw new ServerBackupException("couldn't create directory", e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock getClientLock(String hostName) {
        try {
            return clientLocks.get(hostName);
        } catch (ExecutionException e) {
            throw new ServerBackupException("couldn't get lock from cache", e);
        }
    }
}
