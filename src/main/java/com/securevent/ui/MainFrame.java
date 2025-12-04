package com.securevent.ui;

import javax.swing.*;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("SecureVent - Wellness Journal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null); // Center on screen
        
        // Add our Logic Panel
        add(new JournalPanel());
        
        setVisible(true);
    }
}