import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;



public class MusicInfo {
    class MusicProperty {
        public String title;
        public String author;
        public String duration;
        public String channels;
        public String rate;
        public String bits;
        public String album;
        public String genre;
        public String year;
        public String comment;
    }

    public String title;
    public String author;
    public String duration;
    public String channels;
    public String rate;
    public String bits;
    public String album;
    public String genre;
    public String year;
    public String comment;
    public static ArrayList<MusicProperty> musicInfo;


    public MusicInfo(){
        title = "";
        author = "";
        duration = "";
        channels = "";
        rate = "";
        bits = "";
        album = "";
        genre = "";
        year = "";
        comment = "";
        musicInfo = new ArrayList<MusicProperty>();
    }

    public void getMusicDetail(ArrayList<String> wavFiles) {

        for (int i = 0; i < wavFiles.size(); i++) {
            System.out.println(wavFiles.get(i));
            File file = new File(wavFiles.get(i));
            musicInfo.add(new MusicProperty());

            try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
                AudioFormat format = in.getFormat();
                long frames = in.getFrameLength();
                double durationInSeconds = (frames + 0.0) / format.getFrameRate();



                if(file.getName() != null){
                    musicInfo.get(i).title = file.getName();
                }
                if(String.valueOf(durationInSeconds) != null){
                    musicInfo.get(i).duration = String.valueOf(durationInSeconds);
                }
                if(String.valueOf(format.getProperty("author")) != null){
                    musicInfo.get(i).author =  String.valueOf(format.getProperty("author"));
                }
                if(String.valueOf(format.getChannels()) != null){
                    musicInfo.get(i).channels = String.valueOf(format.getChannels());
                }
                if(String.valueOf(format.getSampleRate()) != null){
                    musicInfo.get(i).rate = String.valueOf(format.getSampleRate());
                }
                if(String.valueOf(format.getSampleSizeInBits()) != null){
                    musicInfo.get(i).bits = String.valueOf(format.getSampleSizeInBits());
                }
                if(format.getProperty("album")!= null){
                    musicInfo.get(i).album = format.getProperty("album").toString();
                }
                if(format.getProperty("genre")!= null){
                    musicInfo.get(i).genre = format.getProperty("genre").toString();
                }
                if(format.getProperty("date") != null){
                    musicInfo.get(i).year = format.getProperty("date").toString();
                }
                if(format.getProperty("comment") != null){
                    musicInfo.get(i).comment = format.getProperty("comment").toString();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void printMusicInfo(){

        for (int i = 0; i < musicInfo.size(); i++) {
            System.out.println("Title: " + musicInfo.get(i).title);
            System.out.println("Duration: " + musicInfo.get(i).duration);
            System.out.println("Author: " + musicInfo.get(i).author);
            System.out.println("Channels: " + musicInfo.get(i).channels);
            System.out.println("Sample Rate: " + musicInfo.get(i).rate);
            System.out.println("Sample Size: " + musicInfo.get(i).bits);
            System.out.println("Album: " + musicInfo.get(i).album);
            System.out.println("Genre: " + musicInfo.get(i).genre);
            System.out.println("Year: " + musicInfo.get(i).year);
            System.out.println("Comment: " +musicInfo.get(i).comment);
        }
    }
    public void saveMusicInfoToFile(String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
//            writer.write("Title,Duration,Author,Channels,Sample Rate,Sample Size,Album,Genre,Year,Comment,\n");
            for (int i = 0; i < musicInfo.size(); i++) {
                writer.write("'" + musicInfo.get(i).title + "',");
                writer.write("'" + musicInfo.get(i).duration + "',");
                writer.write("'" + musicInfo.get(i).author + "',");
                writer.write("'" + musicInfo.get(i).channels + "',");
                writer.write("'" + musicInfo.get(i).rate + "',");
                writer.write("'" + musicInfo.get(i).bits + "',");
                writer.write("'" + musicInfo.get(i).album + "',");
                writer.write("'" + musicInfo.get(i).genre + "',");
                writer.write("'" + musicInfo.get(i).year + "',");
                writer.write("'" + musicInfo.get(i).comment + "',");
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMusicDetail(List<String> wavFiles) {
        musicInfo.clear();
        for (int i = 0; i < wavFiles.size(); i++) {

            String[] tokens = wavFiles.get(i).split(",");
            for (int j = 0; j < tokens.length; j++) {
                tokens[j] = tokens[j].replace("'", "");
            }
            if (tokens.length < 10) {
                continue;
            }
            musicInfo.add(new MusicProperty());
            musicInfo.get(i).title = tokens[0];
            musicInfo.get(i).author = tokens[1];
            musicInfo.get(i).duration = tokens[2];
            musicInfo.get(i).channels = tokens[3];
            musicInfo.get(i).rate = tokens[4];
            musicInfo.get(i).bits = tokens[5];
            musicInfo.get(i).album = tokens[6];
            musicInfo.get(i).genre = tokens[7];
            musicInfo.get(i).year = tokens[8];
            musicInfo.get(i).comment = tokens[9];
        }
    }
    public ArrayList<MusicProperty> getMusicList(){
        return musicInfo;
    }


    public void readMusicInfoFromFile(String filePath) {
        List<String> wavFiles = new ArrayList<>();
        try {
            wavFiles = Files.readAllLines(Paths.get(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setMusicDetail(wavFiles);
    }
    public static void main(String[] args) {
//        ArrayList<String> wavFiles = new ArrayList<String>();
//        wavFiles.add("song1.wav");
//        wavFiles.add("song2.wav");
//
//        MusicInfo musicInfo = new MusicInfo();
//        musicInfo.getMusicDetail(wavFiles);
//        musicInfo.printMusicInfo();
//        musicInfo.saveMusicInfoToFile("music_info.txt");
//        musicInfo.readMusicInfoFromFile("musicInfo.txt");
//        musicInfo.printMusicInfo();
    }
}
