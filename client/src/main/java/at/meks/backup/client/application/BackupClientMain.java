package at.meks.backup.client.application;


import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URI;

import static java.util.Objects.requireNonNull;

@QuarkusMain
public class BackupClientMain implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {

        SystemTray systemTray = SystemTray.getSystemTray();
        Image icon = ImageIO.read(requireNonNull(getClass().getResourceAsStream("/images/tray-icon.png")))
                .getScaledInstance(16, 16, Image.SCALE_DEFAULT);
        PopupMenu popup = new PopupMenu();
        popup.add(menuItem("About", this::openGithubUrl));
        popup.add(menuItem("Exit", this::exitApplication));
        systemTray.add(new TrayIcon(icon, "Backup", popup));
        Quarkus.waitForExit();
        return 0;
    }

    private void exitApplication() {
        Quarkus.asyncExit(0);
    }

    private void openGithubUrl() {
        try {
            Desktop.getDesktop().browse(URI.create("https://github.com/meks77/ClientServerBackup"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MenuItem menuItem(String text, Runnable action) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.addActionListener(e -> action.run());
        return menuItem;
    }

}
