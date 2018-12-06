package at.meks.backupclientserver.backend.services;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;

public class FileStatistics {

    static final FileStatistics NOT_ANALZED = new FileStatistics(-1L, -1L);
    private static final int FACTOR_BYTES_TO_MB = 1024 * 1024;

    private long fileCount = 0;
    private long sizeInBytes = 0;
    private long freeSpaceInBytes = 0;

    FileStatistics() {
    }

    private FileStatistics(long sizeInBytes, long fileCount) {
        this.sizeInBytes = sizeInBytes;
        this.fileCount = fileCount;
    }

    public BigDecimal getSizeInMb() {
        return valueOf(sizeInBytes).divide(valueOf(FACTOR_BYTES_TO_MB), 3, BigDecimal.ROUND_HALF_UP);
    }

    public long getFileCount() {
        return fileCount;
    }

    void incrementFileCount() {
        fileCount++;
    }

    void incrementSizeInBytes(long incrementBy) {
        sizeInBytes+=incrementBy;
    }

    public void setFreeSpaceInBytes(long freeSpaceInBytes) {
        this.freeSpaceInBytes = freeSpaceInBytes;
    }

    public BigDecimal getFreeSpaceInMb() {
        return valueOf(freeSpaceInBytes).divide(valueOf(FACTOR_BYTES_TO_MB), 3, BigDecimal.ROUND_HALF_UP);
    }
}
