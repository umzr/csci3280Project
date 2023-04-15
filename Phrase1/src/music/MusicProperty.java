package music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MusicProperty {
    public String title;
    public float duration;
    public String artist;
    public String album;
    public String genre;
    public String year;
    public String path;
    public String comment;
    public int channels;
    public float rate;
    public int bits;
    public boolean hasLrc;

    public String ftpPath;

    public MusicProperty(String title, float duration, String artist, String album, String genre, String year, String path, String comment, int channels, float rate, int bits, boolean hasLrc, String ftpPath) {
        this.title = title;
        this.duration = duration;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.year = year;
        this.path = path;
        this.comment = comment;
        this.channels = channels;
        this.rate = rate;
        this.bits = bits;
        this.hasLrc = hasLrc;
        this.ftpPath = ftpPath;
    }


    // Empty constructor
    public MusicProperty() {
        title = "";
        duration = 0;
        artist = "";
        album = "";
        genre = "";
        year = "";
        path = "";
        comment = "";
        channels = 0;
        rate = 0;
        bits = 0;
        hasLrc = false;
        ftpPath = "";
    }

    @Override
    public String toString() {
        return title + "|" + duration + "|" + artist + "|" + album + "|" + genre + "|" + year + "|" + path + "|" + comment + "|" + channels + "|" + rate + "|" + bits + "|" + hasLrc + "|" + ftpPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MusicProperty other = (MusicProperty) obj;
        if(!this.title.equals(other.title)) {
            return false;
        }
        if(!this.artist.equals(other.artist))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, artist, album);
    }


    public static byte[] flat2byteMusicProperty(ArrayList<MusicProperty> musicInfo , String clientAddress) {

        for(MusicProperty music : musicInfo) {
            music.ftpPath = clientAddress;
        }
        for(MusicProperty music : musicInfo) {
            System.out.println(music.toString());
        }
        return musicInfo.stream().map(MusicProperty::toString).collect(Collectors.joining(",")).getBytes();
    }

    public static ArrayList<MusicProperty> decodeMusicProperty(byte[] recv) {
        ArrayList<MusicProperty> musicInfo = new ArrayList<>();
        String[] musicInfoString = new String(recv).split(",");
        for (String music : musicInfoString) {
            System.out.println("music: " + music);
            String[] values = music.split("\\|");
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
            try {
                property.hasLrc = Boolean.parseBoolean(values[11].strip());
            } catch (NumberFormatException e) {
                property.hasLrc = false;
            }
            try {
                property.ftpPath = values[12].strip();
            } catch (NumberFormatException e) {
                property.ftpPath = "";
            }
            musicInfo.add(property);


        }
        return musicInfo;
    }
}
