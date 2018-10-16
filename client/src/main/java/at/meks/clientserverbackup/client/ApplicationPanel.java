package at.meks.clientserverbackup.client;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import static java.awt.BorderLayout.CENTER;

class ApplicationPanel extends JPanel {

    ApplicationPanel() {
        super(new BorderLayout());
        buildLayout();
    }

    private void buildLayout() {
        JLabel welcome = new JLabel("Welcome to the Client!");
        add(welcome, CENTER);
    }


}
