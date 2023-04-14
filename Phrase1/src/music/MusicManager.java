package music;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.ArrayList;

public class MusicManager {

    private ArrayList<String> wavFiles = new ArrayList<String>();
    private ArrayList<MusicProperty> musicInfo = new ArrayList<MusicProperty>();

    public ArrayList<MusicProperty> getMusicInfo() {
        return musicInfo;
    }

    public void setMusicInfo(ArrayList<MusicProperty> musicInfo) {
        this.musicInfo = musicInfo;
    }

    public void printMusicInfo(ArrayList<MusicProperty> musicInfo) {
        for (int i = 0; i < musicInfo.size(); i++) {
            System.out.println(musicInfo.get(i).title);
            System.out.println(musicInfo.get(i).duration);
            System.out.println(musicInfo.get(i).artist);
            System.out.println(musicInfo.get(i).album);
            System.out.println(musicInfo.get(i).genre);
            System.out.println(musicInfo.get(i).year);
            System.out.println(musicInfo.get(i).comment);
            System.out.println(musicInfo.get(i).channels);
            System.out.println(musicInfo.get(i).rate);
            System.out.println(musicInfo.get(i).bits);
            System.out.println(musicInfo.get(i).path);
            System.out.println(musicInfo.get(i).hasLrc);
            System.out.println(musicInfo.get(i).ftpPath);
            System.out.println("--------------------");
        }
    }

    private void searchMusicPaths(String root) {
        File file = new File(root);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File f : files) {
                if (f.isDirectory()) {
                    searchMusicPaths(f.getAbsolutePath());
                } else {
                    if (f.getName().endsWith(".wav")) {
                        wavFiles.add(f.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void searchLRC(MusicProperty property) {
        String path = property.path;
        int extIdx = path.lastIndexOf(".");
        String lrcPath;
        if (extIdx == -1) {
            lrcPath = path + ".lrc";
        } else {
            lrcPath = path.substring(0, extIdx + 1) + "lrc";
        }
        File file = new File(lrcPath);
        if (file.exists()) {
            property.hasLrc = true;
        } else {
            property.hasLrc = false;
        }
    }

    private String getString(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    private void retrieveMusicProperty() {

        for (int i = 0; i < wavFiles.size(); i++) {
            System.out.println(wavFiles.get(i));
            File file = new File(wavFiles.get(i));
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                AudioFormat format = audioInputStream.getFormat();

                MusicProperty property = new MusicProperty();
                property.title = file.getName();
                property.artist = getString(format.getProperty("author"));
                property.album = getString(format.getProperty("album"));
                property.genre = getString(format.getProperty("genre"));
                property.year = getString(format.getProperty("date"));
                property.comment = getString(format.getProperty("comment"));
                property.channels = format.getChannels();
                property.rate = format.getSampleRate();
                property.bits = format.getSampleSizeInBits();
                property.path = file.getAbsolutePath();
                property.duration = audioInputStream.getFrameLength() / format.getFrameRate();
                searchLRC(property);

                musicInfo.add(property);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void printMusicPath() {
        for (int i = 0; i < wavFiles.size(); i++) {
            System.out.println(wavFiles.get(i));
            System.out.println("--------------------");
        }
    }

    public void printMusicProperty() {
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
            System.out.println(musicInfo.get(i).hasLrc);
            System.out.println("--------------------");
        }
    }

    public void saveMusicPropertyToFile(String filePath) {
        try(FileWriter writer = new FileWriter(filePath)){
            saveMusicProperty(this.musicInfo, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMusicProperty(ArrayList<MusicProperty> musicInfo, Writer writer){
        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            // write header row
            bufferedWriter.write("title,duration,artist,album,genre,year,path,comment,channels,rate,bits\n");
            // write music properties for each file
            for (MusicProperty property : musicInfo) {
                bufferedWriter.write(property.title + ","
                        + property.duration + ","
                        + property.artist + ","
                        + property.album + ","
                        + property.genre + ","
                        + property.year + ","
                        + property.path + ","
                        + property.comment + ","
                        + property.channels + ","
                        + property.rate + ","
                        + property.bits + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromCsv(String csvPath) {
        try(FileReader reader = new FileReader(csvPath)){
            ArrayList<MusicProperty> musicInfo = loadFromCsv(reader);
            for (MusicProperty property : musicInfo) {
                searchLRC(property);
                this.musicInfo.add(property);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<MusicProperty> loadFromCsv(Reader reader) {
        ArrayList<MusicProperty> musicInfo = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.readLine();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                MusicProperty property = new MusicProperty();
                property.title = values[0];
                property.artist = values[2];
                property.album = values[3];
                property.genre = values[4];
                property.year = values[5];
                property.comment = values[7];
                try{
                    property.channels = Integer.parseInt(values[8].strip());
                } catch (NumberFormatException e) {
                    property.channels = 0;
                }
                try {
                    property.rate = Float.parseFloat(values[9].strip());
                } catch (NumberFormatException e) {
                    property.rate = 0;
                }
                try {
                    property.bits = Integer.parseInt(values[10].strip());
                } catch (NumberFormatException e) {
                    property.bits = 0;
                }
                property.path = values[6];
                try {
                    property.duration = Float.parseFloat(values[1].strip());
                } catch (NumberFormatException e) {
                    property.duration = 0;
                }
                musicInfo.add(property);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return musicInfo;
    }

    public void reload(String rootPath) {
        wavFiles.clear();
        musicInfo.clear();
        searchMusicPaths(rootPath);
        printMusicPath();
        retrieveMusicProperty();
        printMusicProperty();
    }

    public void reload() {
        reload("./src");
    }

    public void run() {
        reload();
        saveMusicPropertyToFile("./music_properties.csv");
    }

    public static void main(String[] args) {
        MusicManager musicManager = new MusicManager();
        musicManager.run();
    }


}
