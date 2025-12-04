package com.securevent.core;

import java.awt.image.BufferedImage;

public class Steganography {

    // Delimiter to know when the message stops (Safety check)
    private static final String END_SIGNAL = "\0\u0004\0";

    // ENCODE: Image + Message -> New Image
    public static BufferedImage embedText(BufferedImage image, String text) {
        String data = text + END_SIGNAL; // Add stopper
        int dataLength = data.length();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        
        // Simple Math check: Do we have enough pixels? (8 bits per char)
        if (dataLength * 8 > imageWidth * imageHeight) {
            throw new IllegalArgumentException("Text is too long for this image!");
        }

        int charIndex = 0;
        int bitIndex = 0; // 0 to 7
        int currentByte = data.charAt(charIndex); // Get ASCII value of char

        // Loop through pixels as a Matrix (x, y)
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                
                // Get current pixel color
                int pixel = image.getRGB(x, y);

                // Math Logic: Extract bit at 'bitIndex' from our char
                // (currentByte >> (7-bitIndex)) shifts the bit we want to the right end
                // & 1 isolates it.
                int bitToHide = (currentByte >> (7 - bitIndex)) & 1;

                // Update Pixel: Clear last bit, then OR with our new bit
                int newPixel = (pixel & 0xFFFFFFFE) | bitToHide;
                
                image.setRGB(x, y, newPixel);

                // Move to next bit
                bitIndex++;
                if (bitIndex >= 8) { // Finished one character?
                    bitIndex = 0;
                    charIndex++;
                    if (charIndex >= dataLength) {
                        return image; // We are done!
                    }
                    currentByte = data.charAt(charIndex);
                }
            }
        }
        return image;
    }

    // DECODE: Image -> Hidden Text
    public static String extractText(BufferedImage image) {
        StringBuilder sb = new StringBuilder();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int currentByte = 0;
        int bitIndex = 0;

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                
                int pixel = image.getRGB(x, y);
                
                // Math Logic: Extract the Last Significant Bit (LSB)
                int lsb = pixel & 1;

                // Add this bit to our current byte builder
                // Shift current value left to make room, then add new bit
                currentByte = (currentByte << 1) | lsb;

                bitIndex++;
                if (bitIndex >= 8) { // We found a full character
                    char c = (char) currentByte;
                    sb.append(c);
                    
                    // Check if we hit the stopper signal
                    if (sb.toString().endsWith(END_SIGNAL)) {
                        // Return text without the signal
                        return sb.substring(0, sb.length() - END_SIGNAL.length());
                    }

                    // Reset for next char
                    bitIndex = 0;
                    currentByte = 0;
                }
            }
        }
        return ""; // Nothing found
    }
}