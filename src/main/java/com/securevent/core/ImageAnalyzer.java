package com.securevent.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ImageAnalyzer {

    // Returns true if SAFE, false if UNSAFE
    public static boolean isImageSafe(File imageFile) {
        try {
            // 1. Setup the Process Call Python
            ProcessBuilder pb = new ProcessBuilder("python", "src/main/resources/analyze_image.py", imageFile.getAbsolutePath());
            pb.redirectErrorStream(true); // Merge errors with output
            
            Process process = pb.start();

            // 2. Read the Result
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine(); // We only expect one line: "SAFE|7.24"
            
            process.waitFor();

            if (line != null && line.startsWith("SAFE")) {
                return true;
            } else {
                System.out.println("Analysis Result: " + line); // Debug
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return true; // Default to allowing it if Python fails
        }
    }
}