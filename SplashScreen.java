import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SplashScreen extends JWindow {
    private BufferedImage image;

    public SplashScreen() {
        // Load the image from the file
        File file = new File("photonSplashScreen.jpg");
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return;
        }

        // Set up a panel to paint the scaled image
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    // Scale the image to fit the window
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        panel.setPreferredSize(new Dimension(600, 400)); // Set preferred window size
        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(null); // Center the window
        setVisible(true);

        // Display for 3 seconds
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            dispose(); // Close splash screen
        }
    }

    public static void main(String[] args) {
        new SplashScreen();
    }
}