package com.securevent.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ImageAnalyzer {

    // Returns true if SAFE, false if UNSAFE
    public static boolean isImageSafe(File imageFile) {
        try {
            // 1. Setup the Process Call Python
           // Merge errors with output
            
            String scriptPath = "scripts" + File.separator + "analyze_image.py";
            File scriptFile = new File(scriptPath);

            ProcessBuilder pb = new ProcessBuilder("python", scriptFile.getAbsolutePath(), imageFile.getAbsolutePath());
            pb.redirectErrorStream(true);            


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
            return false; // Default to failing (unsafe) if Python analysis fails
        }
    }
}