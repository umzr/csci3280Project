
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import javax.sound.sampled.*;

import javax.sound.sampled.*;

public class MusicDecoding {

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
        String test = "src/Album/01. senya — 華鳥風月.wav";
        File file = new File(test);

        try (FileInputStream fis = new FileInputStream(file)) {

            byte[] header = new byte[44]; // the WAV header is 44 bytes long
            fis.read(header);

            // display the WAV header information
            System.out.println("ChunkID: " + new String(header, 0, 4));
            System.out.println("ChunkSize: " + byteArrayToInt(header, 4));
            System.out.println("Format: " + new String(header, 8, 4));
            System.out.println("Subchunk1ID: " + new String(header, 12, 4));
            System.out.println("Subchunk1Size: " + byteArrayToInt(header, 16));
            System.out.println("AudioFormat: " + byteArrayToShort(header, 20));
            System.out.println("NumChannels: " + byteArrayToShort(header, 22));
            System.out.println("SampleRate: " + byteArrayToInt(header, 24));
            System.out.println("ByteRate: " + byteArrayToInt(header, 28));
            System.out.println("BlockAlign: " + byteArrayToShort(header, 32));
            System.out.println("BitsPerSample: " + byteArrayToShort(header, 34));
            System.out.println("Subchunk2ID: " + new String(header, 36, 4));
            System.out.println("Subchunk2Size: " + byteArrayToInt(header, 40));

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("--------------------");
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);

        System.out.println("Audio Format: " + fileFormat.getType());
        System.out.println("Audio Format Encoding: " + audioInputStream.getFormat().getEncoding());
        System.out.println("Audio Sample Rate: " + audioInputStream.getFormat().getSampleRate());
        System.out.println("Audio Channels: " + audioInputStream.getFormat().getChannels());
        System.out.println("Audio Frame Size: " + audioInputStream.getFormat().getFrameSize());
        System.out.println("Audio Frame Rate: " + audioInputStream.getFormat().getFrameRate());
        System.out.println("Audio Sample Size: " + audioInputStream.getFormat().getSampleSizeInBits());
        System.out.println("Audio Big Endian: " + audioInputStream.getFormat().isBigEndian());
        System.out.println("Audio Length: " + audioInputStream.getFrameLength());
        System.out.println(
                "Audio Duration: " + audioInputStream.getFrameLength() / audioInputStream.getFormat().getFrameRate());
        System.out.println("Audio File Size: " + file.length());
        System.out.println("Title: " + fileFormat.properties().get("title"));
        System.out.println("Author: " + fileFormat.properties().get("author"));

        Map<String, Object> properties = fileFormat.properties();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        String author = (String) properties.get("author");
        if (author != null) {
            System.out.println("Author: " + author);
        } else {
            System.out.println("Author not found in metadata");
        }

        // TODO: 
        // try {

        // AudioFileFormat format = AudioSystem.getAudioFileFormat(file);

        // // get the metadata from the format
        // if (format instanceof TAudioFileFormat) {
        // TAudioFileFormat tformat = (TAudioFileFormat) format;
        // Map<?, ?> properties = tformat.properties();
        // String title = (String) properties.get("title");
        // String artist = (String) properties.get("author");
        // String album = (String) properties.get("album");
        // String year = (String) properties.get("date");
        // // print the metadata
        // System.out.println("Title: " + title);
        // System.out.println("Artist: " + artist);
        // System.out.println("Album: " + album);
        // System.out.println("Year: " + year);
        // }
        // } catch (UnsupportedAudioFileException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    private static int byteArrayToInt(byte[] bytes, int offset) {
        return ((bytes[offset + 3] & 0xff) << 24) | ((bytes[offset + 2] & 0xff) << 16)
                | ((bytes[offset + 1] & 0xff) << 8) | (bytes[offset] & 0xff);
    }

    private static short byteArrayToShort(byte[] bytes, int offset) {
        return (short) (((bytes[offset + 1] & 0xff) << 8) | (bytes[offset] & 0xff));
    }
}
