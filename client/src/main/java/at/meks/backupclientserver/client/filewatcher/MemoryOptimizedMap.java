package at.meks.backupclientserver.client.filewatcher;

import at.meks.backupclientserver.client.ClientBackupException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Target: Save memory in the heap
 *
 * How: writing the Path of a WatchKey in a memory mapped file and persisting only the coordinates of the persisted path
 *      in memory.
 *      The coordinates just hold a long for the position and an integer for the length.
 *      Compared to the Path object or the string of the path it saves a lot of memory
 *
 */
class MemoryOptimizedMap {

    private class Coordinate {

        private long position;
        private int length;
    }

    /**
     * using propterties because it needs the less memory.
     * Overall Heap usage when backuping my user.home in windows with 58tsd directories:
     * Hashmap: ~ 250MB
     * IdentityHashMap: ~ 200MB
     * Properties: ~ 150MB
     *
     * e.g. Treemap couldn't be used beause WatchKey doesn't implement Comparable.
     * MapDB couldn't be used because WatchKey needs to be in Memory, visible to the Garbage Collector, otherwise
     * listening to direcories is stopped for those keys.
     */
    private Properties keys = new Properties();
    private FileChannel fileChannel;
    private MappedByteBuffer writeBuffer;
    private static final int BUFFER_SIZE = 8 * 1024;
    private ReentrantLock lock = new ReentrantLock();
    private long absolutePosition = 0;

    MemoryOptimizedMap(File memoryFile) throws IOException {
        fileChannel = new RandomAccessFile(memoryFile, "rw" ).getChannel();
        writeBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, BUFFER_SIZE);
    }

    void put(WatchKey entry, Path value) {
        try {
            lock.lock();
            int position = writeBuffer.position();
            byte[] bytes = value.toString().getBytes();
            if (!writeBuffer.hasRemaining() || writeBuffer.capacity() - position < bytes.length) {
                writeBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, absolutePosition, BUFFER_SIZE);
            }
            writeBuffer.put(bytes);
            keys.put(entry, createCoordinate(absolutePosition, bytes.length));
            absolutePosition += bytes.length;
        } catch (Exception e) {
            throw new ClientBackupException("Error while adding entry to memory map", e);
        } finally {
            lock.unlock();
        }
    }

    private Coordinate createCoordinate(long posiion, int length) {
        Coordinate coordinate = new Coordinate();
        coordinate.position = posiion;
        coordinate.length = length;
        return coordinate;
    }

    Path get(WatchKey entry) {
        String pathString = null;
        Coordinate coordinate = null;
        try {
            lock.lock();
            coordinate = (Coordinate) keys.get(entry);
            if (coordinate != null) {
                MappedByteBuffer readBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, coordinate.position,
                        coordinate.length);
                byte[] targetArray = new byte[coordinate.length];
                readBuffer.get(targetArray);
                pathString = new String(targetArray);
                return Paths.get(pathString);
            }
            return null;
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            Optional.ofNullable(pathString).ifPresent(path -> sb.append(" path: <").append(path).append(">"));
            Optional.ofNullable(coordinate).ifPresent(coords -> sb.append(" pos from ").append(coords.position)
                    .append(" to ").append(coords.position + coords.length));
            throw new ClientBackupException("Error while retrieving entry from memory map. " + sb.toString(), e);
        } finally {
            lock.unlock();
        }
    }

}
