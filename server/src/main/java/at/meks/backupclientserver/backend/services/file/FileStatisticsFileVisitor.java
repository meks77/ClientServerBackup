package at.meks.backupclientserver.backend.services.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

class FileStatisticsFileVisitor extends SimpleFileVisitor<Path> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private FileStatistics statistics;

    static FileStatisticsFileVisitor withStatistics(FileStatistics statistics) {
        FileStatisticsFileVisitor visitor = new FileStatisticsFileVisitor();
        visitor.statistics = statistics;
        return visitor;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if(attrs.isRegularFile()){
            statistics.incrementSizeInBytes(attrs.size());
            statistics.incrementFileCount();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        logger.warn("Couldn't get file statistics of {}. Reason: ", file, exc);
        return FileVisitResult.SKIP_SUBTREE;
    }
}
