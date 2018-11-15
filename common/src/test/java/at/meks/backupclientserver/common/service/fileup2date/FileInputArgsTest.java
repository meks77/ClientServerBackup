package at.meks.backupclientserver.common.service.fileup2date;

import org.junit.jupiter.api.Test;

import static org.fest.assertions.api.Assertions.assertThat;

class FileInputArgsTest {

    @Test
    void whenHashCodeNoExceptionIsThrown() {
        int hash = new FileInputArgs().hashCode();
        assertThat(hash).isGreaterThan(1000);
    }

}