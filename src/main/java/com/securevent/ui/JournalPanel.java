package com.securevent.ui;

import com.securevent.core.AESCrypto;
import com.securevent.core.Steganography;
import com.securevent.utils.ImageUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class JournalPanel extends JPanel {

    private JLabel imagePreviewLabel;
    private JLabel capacityLabel;
    private JTextArea journalInput;
    private JPasswordField passwordField;
    private BufferedImage currentImage;
    private JButton hideBtn;

    public JournalPanel() {
        // 1. MAIN LAYOUT: Split into Left  and Right 
        setLayout(new GridLayout(1, 2, 20, 0)); 
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40)); 
        setBackground(new Color(245, 250, 245)); 

   
        // LEFT PAGE (Image Section)

        JPanel leftPage = new JPanel(new BorderLayout(10, 10));
        leftPage.setOpaque(false);

        // Image Preview (Takes Center of Left Page)
        imagePreviewLabel = new JLabel("No Image Loaded", SwingConstants.CENTER);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(new Color(160, 180, 160), 2));
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(Color.WHITE);
        leftPage.add(imagePreviewLabel, BorderLayout.CENTER);

        // Image Controls (Load Button + Capacity)
        JPanel imgControls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imgControls.setOpaque(false);
        
        JButton loadBtn = new JButton("Load Image");
        styleButton(loadBtn, new Color(100, 180, 120)); // Green Style
        
        capacityLabel = new JLabel("Capacity: Waiting for image...");
        capacityLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        imgControls.add(loadBtn);
        imgControls.add(capacityLabel);
        
        leftPage.add(imgControls, BorderLayout.SOUTH);


        // RIGHT PAGE (Writing Section)
        JPanel rightPage = new JPanel(new BorderLayout(10, 10));
        rightPage.setOpaque(false);

        // Journal Title
        JLabel title = new JLabel("Dear Diary...");
        title.setFont(new Font("Serif", Font.ITALIC, 24));
        title.setForeground(new Color(80, 100, 80));
        rightPage.add(title, BorderLayout.NORTH);

        // The Big Text Area (Takes Center of Right Page)
        journalInput = new JTextArea();
        journalInput.setFont(new Font("Serif", Font.PLAIN, 16));
        journalInput.setLineWrap(true);     
        journalInput.setWrapStyleWord(true); // Don't cut words in half
        
        JScrollPane scrollPane = new JScrollPane(journalInput);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                "Your Thoughts"
        ));
        rightPage.add(scrollPane, BorderLayout.CENTER);

        // Bottom Security Controls (Password + Buttons)
        JPanel securityPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        securityPanel.setOpaque(false);

        // Password Row
        JPanel passRow = new JPanel(new BorderLayout(5, 5));
        passRow.setOpaque(false);
        passRow.add(new JLabel(" Lock Key: "), BorderLayout.WEST);
        passwordField = new JPasswordField();
        passRow.add(passwordField, BorderLayout.CENTER);
        securityPanel.add(passRow);

        // Buttons Row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        
        hideBtn = new JButton("Hide & Save");
        styleButton(hideBtn, new Color(100, 180, 120));
        
        JButton revealBtn = new JButton("Reveal Secret");
        styleButton(revealBtn, new Color(100, 150, 180));

        btnRow.add(hideBtn);
        btnRow.add(revealBtn);
        securityPanel.add(btnRow);

        rightPage.add(securityPanel, BorderLayout.SOUTH);

        // Add Pages to Main Panel
        add(leftPage);
        add(rightPage);

        
        // LOGIC & LISTENERS
       
        
        // 1. Capacity Monitor
        journalInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateCapacity();
            }
        });

        // 2. Load Image Listener
        loadBtn.addActionListener(e -> {
            currentImage = ImageUtils.loadImage(this);
            if (currentImage != null) {
                // Scale for display (Keep aspect ratio if possible, but fit box)
                Image scaled = currentImage.getScaledInstance(
                    imagePreviewLabel.getWidth(), 
                    imagePreviewLabel.getHeight(), 
                    Image.SCALE_SMOOTH
                );
                imagePreviewLabel.setIcon(new ImageIcon(scaled));
                imagePreviewLabel.setText(""); 
                
                updateCapacity();
                
                // Force UI refresh
                revalidate();
                repaint();
            }
        });

        // 3. Hide Logic
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

            String encryptedText = AESCrypto.encrypt(secretText, password);
            try {
                BufferedImage stegoImage = Steganography.embedText(currentImage, encryptedText);
                ImageUtils.saveImage(this, stegoImage);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // 4. Reveal Logic
        revealBtn.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this, "Load the 'Secret' image first!");
                return;
            }
            String password = new String(passwordField.getPassword());

            // Duress Code Check
            if (password.equals("1234")){
                journalInput.setText("My To-Do List:\n1. Drink water\n2. Study Java\n3. Call Mom");
                journalInput.setForeground(new Color(50, 50, 150));
                JOptionPane.showMessageDialog(this, "Journal Unlocked!");
                return;
            }

            String extracted = Steganography.extractText(currentImage);
            if (extracted.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hidden message found.");
                return;
            }

            String decrypted = AESCrypto.decrypt(extracted, password);
            if (decrypted == null) {
                JOptionPane.showMessageDialog(this, "Wrong Password! Access Denied.");
            } else {
                journalInput.setText(decrypted);
                journalInput.setForeground(Color.BLACK);
                JOptionPane.showMessageDialog(this, "Journal Unlocked!");
            }
        });
    }

    // Helper to calculate capacity
    private void updateCapacity() {
        if (currentImage != null) {
            long maxBits = (long) currentImage.getWidth() * currentImage.getHeight();
            long maxChars = (maxBits / 8) - 15;
            int used = journalInput.getText().length();
            capacityLabel.setText("Capacity: " + used + " / " + maxChars + " chars");
            
            if (used > maxChars) {
                capacityLabel.setForeground(Color.RED);
                hideBtn.setEnabled(false);
            } else {
                capacityLabel.setForeground(Color.BLACK);
                hideBtn.setEnabled(true);
            }
        }
    }

    // Helper to style buttons quickly
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }
}