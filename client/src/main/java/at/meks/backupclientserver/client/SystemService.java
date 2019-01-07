package at.meks.backupclientserver.client;

import com.google.inject.Singleton;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Singleton
public class SystemService {

    public String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new ClientBackupException("error while getting the hostname", e);
        }
    }
}
