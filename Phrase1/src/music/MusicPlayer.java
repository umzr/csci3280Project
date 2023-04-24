package music;

import networking.MusicStreamer;
import networking.P2PMusicStreaming;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MusicPlayer {
    private final Supplier<P2PMusicStreaming> networkAppGetter;
    private SourceDataLine dataLine;
    private MusicProperty musicPlaying;
    private InputStream inputStream;
    private MusicStreamer streamer;
    private int currentPos;
    private int bufferSize;
    Object playLock;
    private float volumeDb = 0.0f;
    private FloatControl volumeCtrl;
    private boolean playing = false;
    private CompletableFuture playingTask;

    public MusicPlayer(Supplier<P2PMusicStreaming> networkApp){
        this.networkAppGetter = networkApp;
        bufferSize = 1024;
    }

    public void stop(){
        if(playingTask != null){
            playingTask.cancel(true);
            playingTask = null;
        }
        if(inputStream != null){
            if(streamer != null){
                streamer.close();
                streamer = null;
            }
            else {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {

                }
            }
        }
        playing = false;
        currentPos = 0;
        dataLine.drain();
        dataLine.stop();
        musicPlaying = null;
        volumeCtrl = null;
    }

    public void startPlayMusic(MusicProperty property, boolean isLocal){
        initialize(property, isLocal).thenAccept(success -> {
            if(success) {
                play();
            }
        });
    }

    public MusicProperty getMusicPlaying(){
        return musicPlaying;
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
           playing = true;
           currentPos = 0;
           dataLine.start();
           tryApplyVolume();
           byte[] data = new byte[bufferSize];
           try {
               if(streamer != null){
                   while (streamer.getLastFedChunkNumber() < 5){
                       // wait until the stream has fed with 5 chunks
                       Thread.sleep(500);
                   }
               }
               int dataRead;
               while(true){
                   if(inputStream.available() == 0){
                       if(streamer != null && streamer.getStreamingTask().isDone()) break;
                       else {
                           Thread.sleep(100);
                           continue;
                       }
                   }
                   dataRead = inputStream.read(data);
                   if(dataRead == -1) break;
                   currentPos += dataRead;
                   while(!playing) {
                       Thread.sleep(500);
                   }
                   dataLine.write(data, 0, data.length);
               }
           } catch (IOException e) {

           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           close();
        });
    }

    public float getCurrentTime(){
        return (float) currentPos / (musicPlaying.rate * musicPlaying.channels * musicPlaying.bits / 8);
    }

    public boolean isPlaying() {
        return playing;
    }

    public void pause(){
        playing = false;
    }

    public void resume(){
        playing = true;
    }

    public boolean isOpen(){
        return this.dataLine != null && this.dataLine.isOpen();
    }

    CompletableFuture<Boolean> initialize(MusicProperty property, boolean isLocal){
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, new AudioFormat(property.rate, property.bits, property.channels, property.bits > 8, false));
        try{
            this.dataLine = (SourceDataLine) AudioSystem.getLine(info);
            this.dataLine.open();
            volumeCtrl = (FloatControl) this.dataLine.getControl(FloatControl.Type.MASTER_GAIN);
            musicPlaying = property;
            if(isLocal){
                inputStream = AudioSystem.getAudioInputStream(new File(property.path));
            }
            else{
                P2PMusicStreaming app = this.networkAppGetter.get();
                if(app == null) return CompletableFuture.completedFuture(false);
                return CompletableFuture.supplyAsync(() -> app.getOnlinePeers(app.getTrackerAddress()))
                        .handle((peers, ex) -> {
                            if(ex != null) return false;
                            MusicPlayer.this.streamer = new MusicStreamer(app, property, peers);
                            streamer.streamingJobs();
                            inputStream = streamer.getAudioStream();
                            return true;
                        });
            }
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.completedFuture(true);
    }

    public void close(){
        if(this.dataLine != null){
            this.dataLine.drain();
            this.dataLine.close();
            playing = false;
            musicPlaying = null;
            volumeCtrl = null;
            playingTask = null;
            if(inputStream != null) {
                if(streamer != null){
                    streamer.close();
                    streamer = null;
                }
                else {
                    try {
                        inputStream.close();
                        inputStream = null;
                    } catch (IOException e) {

                    }
                }
            }
        }
    }
}
