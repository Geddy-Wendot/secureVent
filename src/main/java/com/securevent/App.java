package com.securevent;

import com.securevent.ui.MainFrame;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Run UI in the Event Dispatch Thread (Best Practice)
        SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }
}