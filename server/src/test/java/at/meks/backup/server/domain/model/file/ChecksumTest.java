package at.meks.backup.server.domain.model.file;

import lombok.SneakyThrows;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ChecksumTest {

    @Test void bodyIsEmpty() {
        Checksum result = checksumFor("emptyfile.txt");
        assertThat(result).isEqualTo(new Checksum(0L));
    }

    @SneakyThrows
    private Checksum checksumFor(String filePath) {
        return Checksum.forContentOf(uri(filePath));
    }

    private URI uri(String filePath) throws URISyntaxException {
        return Objects.requireNonNull(getClass().getResource(filePath)).toURI();
    }

    @Test void bodyIsNotEmpty() {
        Checksum result = checksumFor("fileWithLittleContent.txt");
        assertThat(result.hash()).isGreaterThan(0L);
    }

    @RepeatedTest(5)
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    @SneakyThrows
    void performancetestWithSmallSourceFiles() {
        Path src = Path.of("src");
        try (Stream<Path> walk = Files.walk(src)) {
            walk.filter(Files::isRegularFile)
                    .forEach(path -> Checksum.forContentOf(path.toUri()));
        }
    }

    @Test void sameContentInDiffentOrderHasDifferentChecksum() {
        assertThat(checksumFor("file_content_01.txt"))
                .isNotEqualTo(checksumFor("file_content_10.txt"));
    }

}