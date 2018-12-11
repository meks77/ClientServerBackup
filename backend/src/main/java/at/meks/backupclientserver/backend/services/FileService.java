package at.meks.backupclientserver.backend.services;

import at.meks.backupclientserver.backend.domain.Client;
import org.apache.commons.io.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class FileService {

    @Inject
    private DirectoryService directoryService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public FileStatistics getBackupFileStatistics() {
        Path backupRootDirectory = directoryService.getBackupRootDirectory();
        FileStatistics statistics = getBackupFileStatistics(backupRootDirectory);
        setFreeSpace(statistics);
        return statistics;
    }

    private void setFreeSpace(FileStatistics statistics) {
        try {
            statistics.setFreeSpaceInBytes(FileSystemUtils.freeSpaceKb() * 1024L);
        } catch (IOException e) {
            logger.error("couldn't get free space statistic", e);
            statistics.setFreeSpaceInBytes(-1L);
        }
    }

    private FileStatistics getBackupFileStatistics(Path path) {
        logger.info("prepare stats of {}", path);
        try {
            FileStatistics statistics = new FileStatistics();
            Files.walkFileTree(path, FileStatisticsFileVisitor.withStatistics(statistics));
            return statistics;
        } catch (IOException e) {
            logger.error("couldn't get file statistics", e);
            return FileStatistics.NOT_ANALYZED;
        }
    }

    public FileStatistics getDiskUsage(Client client) {
        return getBackupFileStatistics(directoryService.getClientPath(client));
    }
}
