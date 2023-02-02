package at.meks.backup.server.domain.model.file;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public record Checksum(long hash) {

    @SneakyThrows
    public static Checksum forContentOf(URI uri) {
        try (CheckedInputStream checkedInputStream = inputStream(uri)) {
            readCompleteFile(checkedInputStream);
            return new Checksum(checkedInputStream.getChecksum().getValue());
        }
    }

    private static CheckedInputStream inputStream(URI uri) throws IOException {
        return new CheckedInputStream(Files.newInputStream(Path.of(uri)), new CRC32());
    }

    private static void readCompleteFile(CheckedInputStream checkedInputStream) throws IOException {
        byte[] buffer = new byte[64];
        int readBytes;
        do {
            readBytes = checkedInputStream.read(buffer, 0, buffer.length);
        } while (readBytes >= 0);
    }
}