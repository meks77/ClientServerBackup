package at.meks.backupclientserver.client;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.fest.assertions.api.Assertions.assertThat;

public class SystemServiceTest {

    private SystemService systemService = new SystemService();

    @Test
    public void testGetHostname() throws UnknownHostException {
        assertThat(systemService.getHostname()).isEqualTo(InetAddress.getLocalHost().getHostName());
    }

    @Test
    public void testIsOsWindows() {
        assertThat(systemService.isOsWindows()).isEqualTo(SystemUtils.IS_OS_WINDOWS);
    }
}