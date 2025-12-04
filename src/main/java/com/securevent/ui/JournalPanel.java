package com.securevent.ui;

import com.securevent.core.AESCrypto;
import com.securevent.core.Steganography;
import com.securevent.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JournalPanel extends JPanel {

    private JLabel imagePreviewLabel;
    private JTextArea journalInput;
    private JPasswordField passwordField;
    private BufferedImage currentImage;

    public JournalPanel() {
        setLayout(new BorderLayout(10, 10)); // Simple layout with gaps
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margins

        
        

        // --- TOP SECTION: Image Preview ---
        imagePreviewLabel = new JLabel("No Image Loaded", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(400, 250));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(new Color(230, 230, 230)); // Light Gray
        add(imagePreviewLabel, BorderLayout.NORTH);

        // --- CENTER SECTION: Controls ---
        JPanel controls = new JPanel(new GridLayout(4, 1, 5, 5));
        
        // 1. Image Buttons
        JPanel imgButtons = new JPanel(new FlowLayout());
        JButton loadBtn = new JButton("Load Image");
        imgButtons.add(loadBtn);
        controls.add(imgButtons);

        // 2. Journal Entry Area
        journalInput = new JTextArea("Write your secret thoughts here...");
        journalInput.setRows(5);
        controls.add(new JScrollPane(journalInput));

        // 3. Password
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.add(new JLabel("Lock Key: "), BorderLayout.WEST);
        passwordField = new JPasswordField();
        passPanel.add(passwordField, BorderLayout.CENTER);
        controls.add(passPanel);

        // 4. Action Buttons
        JPanel actionButtons = new JPanel(new FlowLayout());
        JButton hideBtn = new JButton("Hide & Save");
        JButton revealBtn = new JButton("Reveal Secret");
        hideBtn.setBackground(new Color(100, 200, 100)); // Wellness Green
        revealBtn.setBackground(new Color(100, 150, 200)); // Blue
        actionButtons.add(hideBtn);
        actionButtons.add(revealBtn);
        controls.add(actionButtons);

        add(controls, BorderLayout.CENTER);

       

        // 1. Define a "Wellness Palette"
        Color bgCol = new Color(245, 250, 245); // Very soft mint/white
        Color btnCol = new Color(100, 180, 120); // Calming Green
        Color textCol = new Color(60, 80, 70);   // Dark Green/Grey (easier on eyes)

        // 2. Apply to Main Panel
        setBackground(bgCol);
        journalInput.setFont(new Font("SansSerif", Font.PLAIN, 14));
        journalInput.setForeground(textCol);
        imagePreviewLabel.setBackground(Color.WHITE);

        // 3. Style the buttons (Find where you created hideBtn and revealBtn)
        hideBtn.setBackground(btnCol);
        hideBtn.setForeground(Color.WHITE);
        hideBtn.setFocusPainted(false); // Removes the ugly click border
        hideBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        revealBtn.setBackground(new Color(100, 150, 180)); // Soft Blue
        revealBtn.setForeground(Color.WHITE);
        revealBtn.setFocusPainted(false);
        revealBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        
        // --- LISTENERS (The Logic Links)

        // Load Image Logic
        loadBtn.addActionListener(e -> {
            currentImage = ImageUtils.loadImage(this);
            if (currentImage != null) {
                // Scale image for preview so it fits the box
                Image scaled = currentImage.getScaledInstance(400, 250, Image.SCALE_SMOOTH);
                imagePreviewLabel.setIcon(new ImageIcon(scaled));
                imagePreviewLabel.setText("");
            }
        });

        // HIDE Logic (Write to Image)
        hideBtn.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Please load an image first!");
                return;
            }
            String secretText = journalInput.getText();
            String password = new String(passwordField.getPassword());

            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must set a password!");
                return;
            }

            // 1. Encrypt Text
            String encryptedText = AESCrypto.encrypt(secretText, password);
            
            // 2. Hide in Image
            try {
                BufferedImage stegoImage = Steganography.embedText(currentImage, encryptedText);
                // 3. Save to Disk
                ImageUtils.saveImage(this, stegoImage);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // REVEAL Logic (Read from Image)
        revealBtn.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Load the 'Secret' image first!");
                return;
            }
            String password = new String(passwordField.getPassword());

            // 1. Extract Hidden String
            String extracted = Steganography.extractText(currentImage);
            
            if (extracted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hidden message found.");
                return;
            }

            // 2. Decrypt
            String decrypted = AESCrypto.decrypt(extracted, password);

            if (decrypted == null) {
                JOptionPane.showMessageDialog(this, "Wrong Password! Access Denied.");
            } else {
                journalInput.setText(decrypted);
                JOptionPane.showMessageDialog(this, "Journal Unlocked!");
            }
        });
    }
}