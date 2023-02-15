package at.meks.backup.client.application;


import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import static java.util.Objects.requireNonNull;

@QuarkusMain
public class BackupClientMain implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {

        SystemTray systemTray = SystemTray.getSystemTray();
        Image icon = ImageIO.read(requireNonNull(getClass().getResourceAsStream("/images/tray-icon.png")))
                .getScaledInstance(16, 16, Image.SCALE_DEFAULT);
        systemTray.add(new TrayIcon(icon, "Backup"));
        return 0;
    }

}
