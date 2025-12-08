package com.securevent.ui;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("SecureVent - Private Journal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Add the main journal panel
        add(new JournalPanel());
    }
}