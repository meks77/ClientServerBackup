package at.meks.backupclientserver.client.filewatcher;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MemoryOptimizedMapTest {

    private MemoryOptimizedMap map;

    @Test
    public void givenAddedValueIsReturnedByGet(@TempDir Path tempDir) throws IOException {
        Path tempFile = Files.createTempFile(tempDir, "whatever", ".whenever");
        map = new MemoryOptimizedMap(tempFile.toFile());
        List<Pair<WatchKey, Path>> expectedMapEntries = new LinkedList<>();
        for (int i = 0; i < 6000; i ++) {
            WatchKey key = Mockito.mock(WatchKey.class);
            Path value = Paths.get(getDirectoryName(i));
            expectedMapEntries.add(org.apache.commons.lang3.tuple.Pair.of(key, value));
            map.put(key, value);
        }
        for(int i=0; i < expectedMapEntries.size(); i++) {
            Pair<WatchKey, Path> pair = expectedMapEntries.get(i);
            assertThat(map.get(pair.getKey())).as("entry " + i).isEqualTo(pair.getValue());
        }
    }

    private String getDirectoryName(int counter) {
        return "directory" + counter;
    }

}