package at.meks.backupclientserver.client;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystemServiceTest {

    private SystemService systemService = new SystemService();

    @Test
    public void getHostname() throws UnknownHostException {
        Assertions.assertThat(systemService.getHostname()).isEqualTo(InetAddress.getLocalHost().getHostName());
    }
}