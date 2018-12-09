package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.domain.BackupSet;
import at.meks.backupclientserver.backend.domain.Client;
import at.meks.backupclientserver.backend.services.persistence.ClientRepository;
import at.meks.backupclientserver.common.Md5CheckSumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class DirectoryService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss.SSS");

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Md5CheckSumGenerator md5CheckSumGenerator = new Md5CheckSumGenerator();

    private ReentrantLock createNewBackupSetLock = new ReentrantLock();

    @Inject
    private BackupConfiguration configuration;

    @Inject
    private ClientRepository clientRepository;

    @Inject
    private LockService lockService;

    Path getBackupSetPath(String hostName, String clientBackupSetPath) {
        Client client = clientRepository.getClient(hostName)
                .orElseGet(() -> clientRepository.createNewClient(hostName, md5CheckSumGenerator.md5HexFor(hostName)));
        Path clientPath = getClientPath(client);

        BackupSet backupSet = getBackupSet(clientBackupSetPath, client)
                .orElseGet(() -> createNewBackupSet(client, clientBackupSetPath));
        Path backupSetPath = clientPath.resolve(backupSet.getDirectoryNameOnServer());
        return createIfNotExists(backupSetPath);
    }

    public Path getClientPath(Client client) {
        return getBackupRootDirectory().resolve(client.getDirectoryName());
    }

    private Optional<BackupSet> getBackupSet(String clientBackupSetPath, Client client) {
        return client.getBackupSets().stream()
                .filter(set -> set.getClientBackupSetPath().equals(clientBackupSetPath))
                .findFirst();
    }

    private BackupSet createNewBackupSet(Client client, String clientBackupSetPath) {
        return lockService.runWithLock(createNewBackupSetLock, () -> {
            Optional<BackupSet> backupSet1 = getBackupSet(clientBackupSetPath, client);
            return backupSet1.orElseGet(() -> {
                BackupSet backupSet = new BackupSet();
                backupSet.setDirectoryNameOnServer(md5CheckSumGenerator.md5HexFor(clientBackupSetPath));
                backupSet.setClientBackupSetPath(clientBackupSetPath);
                client.getBackupSets().add(backupSet);
                clientRepository.update(client);
                return backupSet;
            });
        });
    }

    private Path createIfNotExists(Path path) {
        return lockService.runWithLock(lockService.getLockForPath(path),
                () -> {
            if (!path.toFile().exists()) {
                try {
                    logger.info("create directory {}", path);
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new ServerBackupException("couldn't create directory", e);
                }
            }
            return path;
        });
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

    Path getBackupRootDirectory() {
        String applicationRoot = configuration.getApplicationRoot();
        if (applicationRoot == null) {
            throw new ServerBackupException("applicationRoot directory is not set");
        }
        Path backupDir = Paths.get(applicationRoot, "backups");
        return createIfNotExists(backupDir);
    }
}
