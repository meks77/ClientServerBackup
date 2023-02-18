package at.meks.backup.client.application;

import at.meks.backup.client.model.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Slf4j
public class Start {

    private final Config config;
    private final ExitAction exitAction;

    public void start() throws Exception {
        SystemTray systemTray = SystemTray.getSystemTray();
        Image icon = ImageIO.read(requireNonNull(getClass().getResourceAsStream("/images/tray-icon.png")))
                .getScaledInstance(16, 16, Image.SCALE_DEFAULT);
        PopupMenu popup = new PopupMenu();
        popup.add(new MenuItem("ClientId: " + config.clientId()));
        popup.add(menuItem("About", this::openGithubUrl));
        popup.add(menuItem("Exit", exitAction::exit));
        systemTray.add(new TrayIcon(icon, "Backup", popup));
        log.info("Backuped Directories: \n{}", List.of(config.backupedDirectories()));
    }

    private MenuItem menuItem(String text, Runnable action) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.addActionListener(e -> action.run());
        return menuItem;
    }

    private void openGithubUrl() {
        try {
            Desktop.getDesktop().browse(URI.create("https://github.com/meks77/ClientServerBackup"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
