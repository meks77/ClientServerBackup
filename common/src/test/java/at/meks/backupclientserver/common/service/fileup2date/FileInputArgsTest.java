package at.meks.backupclientserver.common.service.fileup2date;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class FileInputArgsTest {

    @Test
    public void whenHashCodeNoExceptionIsThrown() {
        int hash = new FileInputArgs().hashCode();
        assertThat(hash).isGreaterThan(1000);
    }

}