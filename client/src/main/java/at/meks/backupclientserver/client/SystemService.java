package at.meks.backupclientserver.client;

import org.apache.commons.lang3.SystemUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.UnknownHostException;

@ApplicationScoped
public class SystemService {

    public String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new ClientBackupException("error while getting the hostname", e);
        }
    }


    boolean isOsWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    boolean isOsLinux() {
        return SystemUtils.IS_OS_LINUX;
    }
}
