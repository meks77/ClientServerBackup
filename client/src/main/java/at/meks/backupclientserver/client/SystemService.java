package at.meks.backupclientserver.client;

import javax.inject.Named;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Named
public class SystemService {

    public String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new ClientBackupException("error while getting the hostname", e);
        }
    }
}
