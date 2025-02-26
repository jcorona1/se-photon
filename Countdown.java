import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Countdown extends JWindow {
    private BufferedImage image;
    private BufferedImage alertImage;

    public Countdown() {
        // Load the background image from the file
        File file = new File("./countdown_images/background.tif");
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return;
        }

        // Load the alert image from the file
        File alert = new File("./countdown_images/alert-on.tif");
        try {
            alertImage = ImageIO.read(alert);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return;
        }

        // Add the countdown timer
        for (int i=30; i >= 0; i--) {
            // Display the image
            BufferedImage number;
            File count = new File("./countdown_images/"+i+".tif");
            try {
                number = ImageIO.read(count);
            } catch (IOException e) {
                System.err.println("Error loading image: " + e.getMessage());
                return;
            }

            int oscilator = i % 2;

            // Set up a panel to paint the scaled image
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (image != null) {
                        // Scale the image to fit the window
                        g.drawImage(image, 0, 0, 586, 445, this);
                        g.drawImage(number, 171, 204, 246, 111, this);
                        if (oscilator == 0) {
                            g.drawImage(alertImage, 43, 39, 504, 113, this);
                        }
                    }
                }
            };

            panel.setPreferredSize(new Dimension(586, 445)); // Set preferred window size
            getContentPane().add(panel);
            pack();
            setLocationRelativeTo(null); // Center the window
            setVisible(true);

            // Display for 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (i == 0) {
                    dispose(); // Close countdown screen
                }
            }
        }
    }

    public static void main(String[] args) {
        new Countdown();
    }
}