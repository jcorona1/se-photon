// MusicPlayer.java (WAV version using Java Sound API)
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayer {
    private Clip clip;

    public void playRandom() {
        File musicDir = new File("music");
        if (!musicDir.exists() || !musicDir.isDirectory()) {
            System.err.println("Music directory not found.");
            return;
        }

        File[] wavFiles = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        if (wavFiles == null || wavFiles.length == 0) {
            System.err.println("No WAV files found in music directory.");
            return;
        }

        File selectedFile = wavFiles[new Random().nextInt(wavFiles.length)];
        System.out.println("Playing: " + selectedFile.getName());

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(selectedFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    // Call this in PlayerEntry after F5 is pressed
    public static void startMusicWithDelay() {
        MusicPlayer player = new MusicPlayer();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                player.playRandom();
            }
        }, 14000); // 14 seconds delay (timed to start when announcer says "BEGIN!")
    }
}