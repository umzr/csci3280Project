import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;
import java.io.File;
import javax.sound.sampled.*;
import javax.swing.*;

public class MusicControl extends SwingWorker<Void, Void> {
    private int frameSize;
    private File musicFile;
    Clip clip;
    private Thread playbackThread;
    private boolean stopPlayback = false;
    private boolean pausePlayback = false;

    public MusicControl(File musicFile) {
        this.musicFile = musicFile;
    }

    public boolean isRunning() {
        return playbackThread != null && playbackThread.isAlive();
    }

    public void pausePlayback() {
        pausePlayback = true;
    }

    public void stopPlayback() {
        stopPlayback = true;
        if (playbackThread != null) {
            playbackThread.interrupt();
            playbackThread = null;
        }
    }

    public void resumeMusic() {
        pausePlayback = false;
        if (clip != null) {
            clip.start();
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile)) {
                    AudioFormat format = audioInputStream.getFormat();
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                        line.open(format);
                        line.start();

                        int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                        byte[] buffer = new byte[bufferSize];
                        int bytesRead = 0;

                        while (!stopPlayback && bytesRead != -1) {
                            bytesRead = audioInputStream.read(buffer, 0, buffer.length);

                            if (bytesRead >= 0 && !pausePlayback) {
                                line.write(buffer, 0, bytesRead);
                            }
                        }

                        line.drain();
                        line.stop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        playbackThread.start();
        try {
            playbackThread.join();
        } catch (InterruptedException e) {
            // Thread interrupted, do nothing
        }
        return null;
    }
}
