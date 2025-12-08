package com.securevent;

import com.securevent.ui.MainFrame;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Ensure UI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}