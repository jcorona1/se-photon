import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Timer;
import java.util.TimerTask;


public class Countdown extends JWindow {
    static int interval = 30;
    static BufferedImage number;
    static int oscilator;
    private BufferedImage image;
    private BufferedImage alertImage;
    private Timer timer = new Timer();

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
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Display the image
                File count = new File("./countdown_images/"+interval+".tif");
                try {
                    number = ImageIO.read(count);
                } catch (IOException e) {
                    System.err.println("Error loading image: " + e.getMessage());
                    return;
                }    

                oscilator = interval % 2;

                // Set up a panel to paint the scaled image
                JPanel countdownPanel = new JPanel() {
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

                countdownPanel.setPreferredSize(new Dimension(586, 445));
                getContentPane().add(countdownPanel);
                pack();
                setLocationRelativeTo(null); 
                setVisible(true); 

                if (interval == 0) {
                    timer.cancel(); // Close countdown screen
                    dispose();
                }
                interval--;
            }
        }, 1000, 1000); // Display for 1 second      
    }

    public static void main(String[] args) {
        new Countdown();
    }
}
