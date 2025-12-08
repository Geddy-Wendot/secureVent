# SecureVent

### *Privacy is the foundation of Peace of Mind.*

SecureVent is a digital wellness application that allows students to journal without fear of judgment or privacy breaches. It utilizes **Steganography** and **AES Encryption** to conceal text data inside image files (pixels).

## Hackathon Theme: Technology for Wellness
**Problem:** Digital anxiety prevents honest expression. Students fear their journals will be read by others.
**Solution:** A steganographic tool that disguises sensitive journal entries as innocent "Nature Photography."

##  Tech Stack
* **Language:** Java 17+
* **GUI:** Java Swing (Custom AWT Styling)
* **Cryptography:** AES-256-GCM (javax.crypto) with PBKDF2 key derivation
* **Algorithm:** Least Significant Bit (LSB) Manipulation across R, G, and B channels

##  How to Run
1.  Open the project in a Java IDE (e.g., VS Code with Java extensions, IntelliJ).
2.  Ensure you have a JDK 17+ configured.
3.  Run the `main` method in `src/main/java/com/securevent/App.java`.
4.  **To Hide:** Load a PNG -> Type Text -> Set Password -> Click "2. Hide & Save".
4.  **To Reveal:** Load the saved PNG -> Type Password -> Click "Reveal".

##  The Math (Steganography)
We treat the image as a grid of pixels. For each pixel, we alter the Least Significant Bit (LSB) of the Red, Green, and Blue color channels to hide 3 bits of data.
`New_Red = (Old_Red & 0xFE) | Secret_Bit_1`
`New_Green = (Old_Green & 0xFE) | Secret_Bit_2`
`New_Blue = (Old_Blue & 0xFE) | Secret_Bit_3`
This changes each color component by at most 1/255, a modification that is imperceptible to the human eye.


Minimal, up-to-date README for the SecureVent scaffold.

SecureVent is a small Java Swing app that hides encrypted text inside PNG images using LSB steganography and AES encryption.

Project layout (high level)

SecureVent/
- .vscode/                 # VS Code settings (optional)
- lib/                     # Optional: external JARs
- src/
	- main/
		- java/
			- com/securevent/
				- App.java                # Entry point (runs the UI)
				- core/
					- Steganography.java    # LSB embedding/extraction logic
					- AESCrypto.java        # AES helper (encrypt/decrypt)
				- ui/
					- MainFrame.java        # Main application window
					- JournalPanel.java     # Active UI: load/save journal entries inside images
					- GalleryPanel.java     # Deprecated placeholder (kept for history)
					- LoginPanel.java       # Deprecated placeholder (kept for history)
				- utils/
					- ImageUtils.java       # Image load/save helpers
		- resources/
			- icons/
			- images/

Notes and cleanup performed
- `JournalPanel` is the active UI used by `MainFrame`.
- `GalleryPanel` and `LoginPanel` are kept as deprecated placeholders (non-instantiable) to avoid breaking older references; they are safe to remove later.
- The AES implementation is a convenience helper; replace with AES/GCM for production security.
Quick run (simple, no build system)

Compile:
```powershell
javac -d out src/main/java/com/securevent/App.java src/main/java/com/securevent/**/**.java
```

Run:
```powershell
java -cp out com.securevent.App
```
