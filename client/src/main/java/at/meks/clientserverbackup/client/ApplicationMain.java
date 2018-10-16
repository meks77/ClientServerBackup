package at.meks.clientserverbackup.client;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ApplicationMain {

    public static void main(String[] args) {
        final JFrame mainFrame = new JFrame();
        mainFrame.add(new ApplicationPanel());
        mainFrame.setSize(800, 600);
        SwingUtilities.invokeLater(() -> mainFrame.setVisible(true));
    }
}
