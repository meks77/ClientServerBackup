package at.meks.backupclientserver.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.isWritable;

@Singleton
public class FileService {

    private static final String DIRECTORIES_WATCHKEY_FILE_SUFFIX = ".dir";
    private static final String DIRECTORIES_WATCHKEY_FILE_PREFIX = "directoriesWatchKey";

    private ReentrantLock configInitLock = new ReentrantLock();

    @Inject
    private ConfigFileInitializer configFileInitializer;

    Path getConfigFile() {
        return rethrowException(() -> {
            Path configFile = getApplicationDirectory().resolve(".config");
            configInitLock.lock();
            try {
                if (!configFile.toFile().exists()) {
                    createFile(configFile);
                    configFileInitializer.initializeConfigFile(configFile);
                }
                return configFile;
            } finally {
              configInitLock.unlock();
            }
        });
    }

    private <R> R rethrowException(Callable<R> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new ClientBackupException(e);
        }
    }

    private Path getApplicationDirectory() throws IOException {
        return Files.createDirectories(Paths.get(System.getProperty("user.home"),".ClientServerBackup"));
    }

    public void cleanupDirectoriesMapFiles() {
        rethrowException(() -> {
            Path applicationDirectory = getApplicationDirectory();
            Files.newDirectoryStream(applicationDirectory,
                    this::isDirectoryWatchKeyDirFile)
                .forEach(this::deleteSilently);
            return Void.TYPE;
        });
    }

    private boolean isDirectoryWatchKeyDirFile(Path path) {
        String fileName = path.getFileName().toString();
        return isWritable(path) && path.toFile().isFile()
                && fileName.endsWith(DIRECTORIES_WATCHKEY_FILE_SUFFIX) && fileName.startsWith(DIRECTORIES_WATCHKEY_FILE_PREFIX);
    }

    private void deleteSilently(Path path) {
        rethrowException(() -> Files.deleteIfExists(path));
    }

    public Path getDirectoriesMapFile() {
        return rethrowException(() -> createTempFile(getApplicationDirectory(), DIRECTORIES_WATCHKEY_FILE_PREFIX,
                DIRECTORIES_WATCHKEY_FILE_SUFFIX));
    }
}
