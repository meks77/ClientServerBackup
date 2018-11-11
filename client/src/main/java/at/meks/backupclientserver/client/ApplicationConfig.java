package at.meks.backupclientserver.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ApplicationConfig {

    Path[] getBackupedDirs() {
        try {
            Properties configProps = new Properties();
            configProps.load(new FileInputStream(Paths.get(System.getProperty("user.home"),
                    ".ClientServerBackup",".config").toFile()));
            return configProps.stringPropertyNames().stream()
                    .filter(s -> s.startsWith("backupset.dir"))
                    .map(configProps::get)
                    .map(o -> (String) o)
                    .map(Paths::get)
                    .toArray(Path[]::new);
        } catch (IOException e) {
            throw new ClientBackupException("couldn't read config file", e);
        }
    }

    public String getServerHost() {
//        return "10.0.0.101";
        return "localhost";
    }

    public int getServerPort() {
        return 8080;
    }

}
