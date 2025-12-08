package com.securevent.ui;

import com.securevent.core.AESCrypto;
import com.securevent.core.Steganography;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class JournalPanel extends JPanel {
    // UI Components
    private JTextArea textArea;
    private JPasswordField passField;
    private JLabel statusLabel;
    private JButton loadBtn, saveBtn, revealBtn;
    private ImagePanel imagePreviewPanel; // Custom panel to render image

    // Logic Data
    private BufferedImage currentImage;
    private File currentFile;

    // Theme Colors
    private final Color BG_COLOR = new Color(40, 44, 52); // Dark Slate
    private final Color TEXT_COLOR = new Color(220, 223, 228); // Off-white
    private final Color ACCENT_COLOR = new Color(97, 175, 239); // Soft Blue
    private final Color SUCCESS_COLOR = new Color(152, 195, 121); // Soft Green
    private final Color ERROR_COLOR = new Color(224, 108, 117); // Soft Red

    public JournalPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("SecureVent Journal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(ACCENT_COLOR);
        headerPanel.add(title);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center: Split Pane (Text Left | Image Right) ---
        
        // 1. Text Area (Left)
        textArea = new JTextArea("Write your thoughts here... \n\n(Load an image on the right to hide this text inside it.)");
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setBackground(new Color(33, 37, 43));
        textArea.setForeground(TEXT_COLOR);
        textArea.setCaretColor(ACCENT_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        // 2. Image Preview (Right)
        imagePreviewPanel = new ImagePanel();
        imagePreviewPanel.setBackground(new Color(33, 37, 43));
        imagePreviewPanel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        // 3. The Splitter
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, textScroll, imagePreviewPanel);
        splitPane.setResizeWeight(0.5); // Give equal space initially
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        splitPane.setBackground(BG_COLOR);
        
        add(splitPane, BorderLayout.CENTER);

        // --- Bottom: Controls ---
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setOpaque(false);

        // Password Row
        JPanel passPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passPanel.setOpaque(false);
        JLabel passLabel = new JLabel("Encryption Password: ");
        passLabel.setForeground(TEXT_COLOR);
        passField = new JPasswordField(20);
        passPanel.add(passLabel);
        passPanel.add(passField);

        // Buttons Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);
        
        loadBtn = createStyledButton("1. Load Image");
        saveBtn = createStyledButton("2. Hide & Save");
        revealBtn = createStyledButton("3. Reveal Text");

        btnPanel.add(loadBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(revealBtn);

        // Status Bar
        statusLabel = new JLabel("Ready. Load an image to start.");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        bottomContainer.add(passPanel);
        bottomContainer.add(btnPanel);
        bottomContainer.add(statusLabel);

        add(bottomContainer, BorderLayout.SOUTH);

        // --- Event Listeners ---
        loadBtn.addActionListener(e -> loadImage());
        saveBtn.addActionListener(e -> hideAndSave());
        revealBtn.addActionListener(e -> revealText());
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(BG_COLOR);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        return btn;
    }

    // --- Logic Methods ---

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = chooser.getSelectedFile();
                currentImage = ImageIO.read(currentFile);
                
                // Update the visual preview
                imagePreviewPanel.setImage(currentImage);
                imagePreviewPanel.repaint();
                
                setStatus("Image Loaded: " + currentFile.getName(), SUCCESS_COLOR);
            } catch (Exception e) {
                setStatus("Error loading image.", ERROR_COLOR);
            }
        }
    }

    private void hideAndSave() {
        // Run on a background thread to avoid freezing the UI
        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                if (currentImage == null) {
                    throw new IllegalStateException("Please load an image first.");
                }
                String text = textArea.getText();
                char[] passwordChars = passField.getPassword();

                if (text.isEmpty() || passwordChars.length == 0) {
                    throw new IllegalStateException("Text and Password are required.");
                }

                // Capacity Check to prevent data loss
                long maxBytes = ((long) currentImage.getWidth() * currentImage.getHeight() * 3 / 8) - 4; // -4 for length header
                if (text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > maxBytes) {
                    throw new IllegalStateException("Text is too large for the selected image's capacity.");
                }

                setStatus("Encrypting and embedding...", ACCENT_COLOR);
                String encrypted = AESCrypto.encrypt(text, new String(passwordChars));
                java.util.Arrays.fill(passwordChars, ' '); // Clear password from memory

                BufferedImage stegImage = Steganography.embed(currentImage, encrypted);

                JFileChooser chooser = new JFileChooser();
                if (chooser.showSaveDialog(JournalPanel.this) == JFileChooser.APPROVE_OPTION) {
                    File output = chooser.getSelectedFile();
                    if (!output.getName().toLowerCase().endsWith(".png")) {
                        output = new File(output.getParent(), output.getName() + ".png");
                    }
                    ImageIO.write(stegImage, "png", output);
                    return output;
                }
                return null; // Save was cancelled
            }

            @Override
            protected void done() {
                try {
                    File savedFile = get();
                    if (savedFile != null) {
                        setStatus("Success! Journal saved to " + savedFile.getName(), SUCCESS_COLOR);
                    } else {
                        setStatus("Save cancelled.", Color.GRAY);
                    }
                } catch (Exception e) {
                    String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    setStatus("Error: " + message, ERROR_COLOR);
                }
            }
        }.execute();
    }

    private void revealText() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                if (currentImage == null) {
                    throw new IllegalStateException("Please load an image first.");
                }
                char[] passwordChars = passField.getPassword();
                if (passwordChars.length == 0) {
                    throw new IllegalStateException("Password is required.");
                }

                setStatus("Extracting and decrypting...", ACCENT_COLOR);
                String encrypted = Steganography.extract(currentImage);
                if (encrypted == null) {
                    throw new IllegalStateException("No hidden data found or image is corrupt.");
                }

                String decrypted = AESCrypto.decrypt(encrypted, new String(passwordChars));
                java.util.Arrays.fill(passwordChars, ' '); // Clear password from memory
                return decrypted;
            }

            @Override
            protected void done() {
                try {
                    String decryptedText = get();
                    textArea.setText(decryptedText);
                    setStatus("Decryption Successful!", SUCCESS_COLOR);
                } catch (Exception e) {
                    // GCM authentication failure (wrong password) throws AEADBadTagException
                    if (e.getCause() instanceof javax.crypto.AEADBadTagException) {
                        setStatus("Access Denied: Wrong Password or corrupt data.", ERROR_COLOR);
                    } else {
                        String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        setStatus("Error: " + message, ERROR_COLOR);
                    }
                    textArea.setText(""); // Clear text area on failure
                }
            }
        }.execute();
    }

    /**
     * Inner class to handle Image Drawing and Scaling
     */
    class ImagePanel extends JPanel {
        private BufferedImage img;

        public void setImage(BufferedImage img) {
            this.img = img;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                // Calculate scaling logic to fit image within panel (Aspect Ratio preserved)
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int panelW = getWidth();
                int panelH = getHeight();
                int imgW = img.getWidth();
                int imgH = img.getHeight();

                double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
                int newW = (int) (imgW * scale);
                int newH = (int) (imgH * scale);
                
                // Center the image
                int x = (panelW - newW) / 2;
                int y = (panelH - newH) / 2;

                g2.drawImage(img, x, y, newW, newH, null);
            } else {
                // Draw placeholder text if no image
                g.setColor(Color.GRAY);
                g.drawString("No Image Loaded", getWidth() / 2 - 50, getHeight() / 2);
            }
        }
    }
}