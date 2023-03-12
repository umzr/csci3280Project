import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MusicManagement {
    class MusicProperty {
        public String title;
        public String duration;
        public String artist;
        public String album;
        public String genre;
        public String year;
        public String path;
        public String comment;
        public String channels;
        public String rate;
        public String bits;
        public String IsLrc;

    }

    public ArrayList<String> wavFiles = new ArrayList<String>();
    public ArrayList<MusicProperty> musicInfo = new ArrayList<MusicProperty>();

    public void print(String message){
        System.out.println(message);
    }

    public void SearchMusicPaths(String path){
        File file = new File(path);
        File[] files = file.listFiles();
        for (File f : files){
            if (f.isDirectory()){
                SearchMusicPaths(f.getAbsolutePath());
            }else{
                if (f.getName().endsWith(".wav")){
                    wavFiles.add(f.getAbsolutePath());
                }
            }
        }
    }

    public void searchLRC(MusicProperty property){
        String path = property.path;
        String lrcPath = path.substring(0,path.length()-3) + "lrc";
        File file = new File(lrcPath);
        if (file.exists()){
            property.IsLrc = "true";
        }else{
            property.IsLrc = "false";
        }

    }
    public void getMusicProperty() {

        for (int i = 0; i < wavFiles.size(); i++) {
            System.out.println(wavFiles.get(i));
            File file = new File(wavFiles.get(i));
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                AudioFormat format = audioInputStream.getFormat();

                MusicProperty property = new MusicProperty();
                property.title = file.getName();
                property.artist = String.valueOf(format.getProperty("author"));
                property.album =  String.valueOf(format.getProperty("album"));
                property.genre =  String.valueOf(format.getProperty("genre"));
                property.year =  String.valueOf(format.getProperty("date"));
                property.comment = String.valueOf(format.getProperty("comment"));
                property.channels = Integer.toString(format.getChannels());
                property.rate = Float.toString(format.getSampleRate());
                property.bits = Integer.toString(format.getSampleSizeInBits());
                property.path = file.getAbsolutePath();
                property.duration = Long.toString(audioInputStream.getFrameLength() / (long) format.getFrameRate());
                searchLRC(property);

                musicInfo.add(property);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void printMusicPath(){
        for (int i = 0; i < wavFiles.size(); i++) {
            System.out.println(wavFiles.get(i));
            System.out.println("--------------------");
        }
    }

    public void printMusicProperty(){
        for (int i = 0; i < musicInfo.size(); i++) {
            System.out.println(musicInfo.get(i).title);
            System.out.println(musicInfo.get(i).duration);
            System.out.println(musicInfo.get(i).artist);
            System.out.println(musicInfo.get(i).album);
            System.out.println(musicInfo.get(i).genre);
            System.out.println(musicInfo.get(i).year);
            System.out.println(musicInfo.get(i).path);
            System.out.println(musicInfo.get(i).comment);
            System.out.println(musicInfo.get(i).channels);
            System.out.println(musicInfo.get(i).rate);
            System.out.println(musicInfo.get(i).bits);
        }
    }

    public void saveMusicPropertyToFile(String filePath){
        try {
            FileWriter writer = new FileWriter(filePath);

            // write header row
            writer.write("title, duration, artist, album, genre, year, path, comment, channels, rate, bits, LRC\n");

            // write music properties for each file
            for (int i = 0; i < musicInfo.size(); i++) {
                MusicProperty property = musicInfo.get(i);
                writer.write(property.title + ", " + property.duration + ", " + property.artist + ", " + property.album + ", " + property.genre + ", " + property.year + ", " + property.path + ", " + property.comment + ", " + property.channels + ", " + property.rate + ", " + property.bits + ", " + property.IsLrc + "\n");

            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String FolderPath = "./src";
        SearchMusicPaths(FolderPath);
        printMusicPath();
        getMusicProperty();
        printMusicProperty();
        saveMusicPropertyToFile("./music_properties.csv");
    }

    public static void main(String[] args) {
        MusicManagement musicManagement = new MusicManagement();
        musicManagement.run();
    }
}
