package com.securevent.core;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

public class Steganography {

    // Embeds the message string into the image
    public static BufferedImage embed(BufferedImage image, String message) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        int len = messageBytes.length;
        
        // 4 bytes for length + message bytes
        byte[] dataToHide = new byte[4 + len];
        
        // Encode length in first 4 bytes
        dataToHide[0] = (byte) ((len >> 24) & 0xFF);
        dataToHide[1] = (byte) ((len >> 16) & 0xFF);
        dataToHide[2] = (byte) ((len >> 8) & 0xFF);
        dataToHide[3] = (byte) (len & 0xFF);
        
        System.arraycopy(messageBytes, 0, dataToHide, 4, len);

        return embedBytes(image, dataToHide);
    }

    // Extracts the message string from the image
    public static String extract(BufferedImage image) {
        byte[] lengthBytes = extractBytes(image, 4);
        if (lengthBytes == null) return null;

        int len = ((lengthBytes[0] & 0xFF) << 24) |
                  ((lengthBytes[1] & 0xFF) << 16) |
                  ((lengthBytes[2] & 0xFF) << 8) |
                  (lengthBytes[3] & 0xFF);

        // Safety check: Don't try to allocate massive arrays if reading garbage
        if (len < 0 || len > image.getWidth() * image.getHeight() * 3 / 8) {
            throw new IllegalArgumentException("Invalid data detected or image empty.");
        }

        // We've read 32 bits (4 bytes). We need to continue reading 'len' bytes *after* that.
        // The extractBytes helper starts from 0, so we need a way to read with offset or read all.
        // For simplicity, we re-read everything including header, then slice. 
        // A optimized version would maintain a pixel cursor.
        
        byte[] allData = extractBytes(image, 4 + len);
        return new String(allData, 4, len, StandardCharsets.UTF_8);
    }

    private static BufferedImage embedBytes(BufferedImage image, byte[] data) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        int dataIndex = 0;
        int bitIndex = 0; // 0 to 7

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (dataIndex >= data.length) return image;

                int pixel = image.getRGB(x, y);
                // Extract channels
                int alpha = (pixel >> 24) & 0xFF;
                int red   = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue  = pixel & 0xFF;

                // Embed in Red, then Green, then Blue
                for (int i = 0; i < 3; i++) {
                    if (dataIndex < data.length) {
                        int bit = (data[dataIndex] >> (7 - bitIndex)) & 1;
                        
                        if (i == 0) red = (red & 0xFE) | bit;
                        if (i == 1) green = (green & 0xFE) | bit;
                        if (i == 2) blue = (blue & 0xFE) | bit;

                        bitIndex++;
                        if (bitIndex == 8) {
                            bitIndex = 0;
                            dataIndex++;
                        }
                    }
                }
                
                int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newPixel);
            }
        }
        return image;
    }

    private static byte[] extractBytes(BufferedImage image, int lengthToRead) {
        byte[] data = new byte[lengthToRead];
        int width = image.getWidth();
        int height = image.getHeight();

        int dataIndex = 0;
        int bitIndex = 0;
        int currentByte = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (dataIndex >= lengthToRead) return data;

                int pixel = image.getRGB(x, y);
                int red   = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue  = pixel & 0xFF;

                int[] channels = {red, green, blue};

                for (int i = 0; i < 3; i++) {
                    if (dataIndex < lengthToRead) {
                        int bit = channels[i] & 1;
                        currentByte = (currentByte << 1) | bit;
                        bitIndex++;

                        if (bitIndex == 8) {
                            data[dataIndex] = (byte) currentByte;
                            dataIndex++;
                            bitIndex = 0;
                            currentByte = 0;
                        }
                    }
                }
            }
        }
        return data;
    }
}