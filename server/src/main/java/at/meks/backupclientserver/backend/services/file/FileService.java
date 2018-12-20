package at.meks.backupclientserver.backend.services.file;

import at.meks.backupclientserver.backend.domain.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class FileService {

    @Autowired
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
            statistics.setFreeSpaceInBytes(
                    Files.getFileStore(directoryService.getBackupRootDirectory()).getUsableSpace());
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

    public Path createFileWithRandomName(Path parentDir) throws IOException {
        return Files.createTempFile(parentDir, "errorStack", ".txt");
    }
}
