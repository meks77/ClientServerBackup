package at.meks.backupclientserver.backend.services.file;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

public class FileStatisticsTest {

    private FileStatistics fileStatistics = new FileStatistics();

    @Test
    public void given15Comma4656MbFreeSpaceWhenGetSizeInMbThen15Comma466IsReturned() {
        fileStatistics.setFreeSpaceInBytes((long)(15.4656 * 1024.0 * 1024.0));
        assertThat(fileStatistics.getFreeSpaceInMb()).isEqualTo(
                valueOf(15.466).setScale(3, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    public void whenIncrementFileCountThenGetFileCountReturnsIncrementedValue() {
        fileStatistics.incrementFileCount();
        assertThat(fileStatistics.getFileCount()).isEqualTo(1);
        fileStatistics.incrementFileCount();
        assertThat(fileStatistics.getFileCount()).isEqualTo(2);
        fileStatistics.incrementFileCount();
        assertThat(fileStatistics.getFileCount()).isEqualTo(3);
    }

    @Test
    public void whenIncrementingSizeInBytesThenGetSizeInMbReturnsTheSumOfIncrmentedBytes() {
        long[] byteSizeValues = new long[]{854L, 756L, 17472L};
        long expectedBytes = Arrays.stream(byteSizeValues).sum();
        BigDecimal expectedSizeInMb = valueOf(expectedBytes)
                .divide(valueOf(1024L * 1024L), 3, ROUND_HALF_UP);

        fileStatistics.incrementSizeInBytes(byteSizeValues[0]);
        fileStatistics.incrementSizeInBytes(byteSizeValues[1]);
        fileStatistics.incrementSizeInBytes(byteSizeValues[2]);

        assertThat(fileStatistics.getSizeInMb()).isEqualTo(expectedSizeInMb);
    }

}
