package at.meks.backupclientserver.backend.services.file;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;

public class FileStatistics {

    public static final FileStatistics NOT_ANALYZED = new FileStatistics(-1L, -1L);
    private static final int FACTOR_BYTES_TO_MB = 1024 * 1024;

    @Getter
    private long fileCount = 0;
    private long sizeInBytes = 0;
    @Setter
    private long freeSpaceInBytes = 0;

    FileStatistics() {
    }

    private FileStatistics(long sizeInBytes, long fileCount) {
        this.sizeInBytes = sizeInBytes;
        this.fileCount = fileCount;
    }

    @SuppressWarnings("WeakerAccess")
    public BigDecimal getSizeInMb() {
        return valueOf(sizeInBytes).divide(valueOf(FACTOR_BYTES_TO_MB), 3, BigDecimal.ROUND_HALF_UP);
    }

    @SuppressWarnings("WeakerAccess")
    public BigDecimal getFreeSpaceInMb() {
        return valueOf(freeSpaceInBytes).divide(valueOf(FACTOR_BYTES_TO_MB), 3, BigDecimal.ROUND_HALF_UP);
    }

    void incrementFileCount() {
        fileCount++;
    }

    void incrementSizeInBytes(long incrementBy) {
        sizeInBytes+=incrementBy;
    }
}
