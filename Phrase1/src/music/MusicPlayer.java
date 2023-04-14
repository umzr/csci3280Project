package music;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class MusicPlayer {
    private SourceDataLine dataLine;
    private MusicProperty musicPlaying;
    private InputStream inputStream;
    private int currentPos;
    private int bufferSize;
    Object playLock;
    private float volumeDb = 0.0f;
    private FloatControl volumeCtrl;
    private boolean playing = false;
    private CompletableFuture playingTask;

    public MusicPlayer(){
        bufferSize = 44100;
    }

    public void stop(){
        playing = false;
        dataLine.drain();
        dataLine.stop();
        currentPos = 0;
        musicPlaying = null;
        volumeCtrl = null;
        try {
            inputStream.close();
            inputStream = null;
        } catch (IOException e) {

        }
    }

    public void startPlayMusic(MusicProperty property, boolean isLocal){
        initialize(property, isLocal);
        play();
    }

    public void setVolume(float volumeLinear){
        if(volumeLinear <= 0){
            volumeDb = -10000;
        }
        else{
            volumeDb = (float) (20 * Math.log10(volumeLinear));
        }
        tryApplyVolume();
    }

    public void tryApplyVolume(){
        if(volumeCtrl != null){
            volumeDb = Math.max(volumeCtrl.getMinimum(), volumeDb);
            volumeCtrl.setValue(volumeDb);
        }
    }

    public void play(){
        playingTask = CompletableFuture.runAsync(() -> {
            playLock = new Object();

            synchronized (playLock){
               playing = true;
               currentPos = 0;
               dataLine.start();
               byte[] data = new byte[bufferSize];
               try {
                   int dataRead;
                   while((dataRead = inputStream.read(data)) != -1){
                       currentPos += dataRead;
                       while(!playing) playLock.wait();
                       dataLine.write(data, 0, data.length);
                   }
               } catch (IOException e) {

               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               close();
            }
        });
    }

    public float getCurrentTime(){
        return (float) currentPos / (musicPlaying.rate * musicPlaying.channels * musicPlaying.bits / 8);
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isOpen(){
        return this.dataLine != null && this.dataLine.isOpen();
    }

    void initialize(MusicProperty property, boolean isLocal){
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, new AudioFormat(property.rate, property.bits, property.channels, property.bits > 8, false));
        try{
            if(isLocal){
                inputStream = AudioSystem.getAudioInputStream(new File(property.path));
            }
            this.dataLine = (SourceDataLine) AudioSystem.getLine(info);
            this.dataLine.open();
            volumeCtrl = (FloatControl) this.dataLine.getControl(FloatControl.Type.MASTER_GAIN);
            musicPlaying = property;
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        if(this.dataLine != null){
            this.dataLine.drain();
            this.dataLine.close();
            playing = false;
            musicPlaying = null;
            volumeCtrl = null;
            if(inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {

                }
            }
        }
    }
}
