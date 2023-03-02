package at.meks.backup.shared.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;
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
        return requireNonNull(getClass().getResource(filePath)).toURI();
    }

    @Test void bodyIsNotEmpty() {
        Checksum result = checksumFor("fileWithLittleContent.txt");
        assertThat(result.hash()).isGreaterThan(0L);
    }

    @Test void sameContentInDiffentOrderHasDifferentChecksum() {
        assertThat(checksumFor("file_content_01.txt"))
                .isNotEqualTo(checksumFor("file_content_10.txt"));
    }

}