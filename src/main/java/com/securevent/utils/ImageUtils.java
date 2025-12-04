package com.securevent.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    // Helper to open a file chooser and load an image
    public static BufferedImage loadImage(JComponent parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Image (PNG only recommended)");
        // Filter for images
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                return ImageIO.read(selectedFile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Error loading image!");
            }
        }
        return null;
    }

    // Helper to save the modified image
    public static void saveImage(JComponent parent, BufferedImage image) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Your Secure Journal");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure it ends with .png (Crucial for steganography!)
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
            }
            try {
                ImageIO.write(image, "png", fileToSave);
                JOptionPane.showMessageDialog(parent, "Journal Saved Successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, "Error saving image!");
            }
        }
    }
}