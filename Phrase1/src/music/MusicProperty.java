package music;

import java.util.ArrayList;
import java.util.Arrays;
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

    public MusicProperty(String title, float duration, String artist, String album, String genre, String year, String path, String comment, int channels, float rate, int bits, boolean hasLrc) {
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
    }

    @Override
    public String toString() {
        return title + "|" + duration + "|" + artist + "|" + album + "|" + genre + "|" + year + "|" + path + "|" + comment + "|" + channels + "|" + rate + "|" + bits + "|" + hasLrc;
    }

    public static byte[] flat2byteMusicProperty(ArrayList<MusicProperty> musicInfo) {
        return musicInfo.stream().map(MusicProperty::toString).collect(Collectors.joining(",")).getBytes();
    }

    public static ArrayList<MusicProperty> decodeMusicProperty(byte[] recv) {
        ArrayList<MusicProperty> musicInfo = new ArrayList<>();
        String[] musicInfoString = new String(recv).split(",");
        for (String music : musicInfoString) {
            System.out.println("music: " + music);
            String[] musicProperty = music.split("\\|");
            System.out.println(Arrays.toString(musicProperty));
//            musicInfo.add(new MusicProperty(musicProperty[0], Float.parseFloat(musicProperty[1]), musicProperty[2], musicProperty[3], musicProperty[4], musicProperty[5], musicProperty[6], musicProperty[7], Integer.parseInt(musicProperty[8]), Float.parseFloat(musicProperty[9]), Integer.parseInt(musicProperty[10]), Boolean.parseBoolean(musicProperty[11])));
            MusicProperty property = new MusicProperty();
            property.title = musicProperty[0].isEmpty() ? null : musicProperty[0];
            property.duration = Float.parseFloat(musicProperty[1]);
            property.artist = musicProperty[2].isEmpty() ? null : musicProperty[2];
            property.album = musicProperty[3].isEmpty() ? null : musicProperty[3];
            property.genre = musicProperty[4].isEmpty() ? null : musicProperty[4];
            property.year = musicProperty[5].isEmpty() ? null : musicProperty[5];
            property.path = musicProperty[6].isEmpty() ? null : musicProperty[6];
            property.comment = musicProperty[7].isEmpty() ? null : musicProperty[7];
            property.channels = Integer.parseInt(musicProperty[8]);
            property.rate = Float.parseFloat(musicProperty[9]);
            property.bits = Integer.parseInt(musicProperty[10]);
            property.hasLrc = Boolean.parseBoolean(musicProperty[11]);

            musicInfo.add(property);
        }
        return musicInfo;
    }
}
