import java.io.*;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.io.File;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;

public class DecodingPlayback {

    class decodeProporty{
        public String chunkID;
        public int chunkSize;
        public String format;
        public String subchunk1ID;
        public int subchunk1Size;
        public int audioFormat;
        public int numChannels;
        public int sampleRate;
        public int byteRate;
        public int blockAlign;
        public int bitsPerSample;
        public String subchunk2ID;
        public int subchunk2Size;
    }
    public String chunkID;
    public int chunkSize;
    public String format;
    public String subchunk1ID;
    public int subchunk1Size;
    public int audioFormat;
    public int numChannels;
    public int sampleRate;
    public int byteRate;
    public int blockAlign;
    public int bitsPerSample;
    public String subchunk2ID;
    public int subchunk2Size;
    public static ArrayList<decodeProporty> decodeInfo;

    public DecodingPlayback() {
        chunkID = "";
        chunkSize = 0;
        format = "";
        subchunk1ID = "";
        subchunk1Size = 0;
        audioFormat = 0;
        numChannels = 0;
        sampleRate = 0;
        byteRate = 0;
        blockAlign = 0;
        bitsPerSample = 0;
        subchunk2ID = "";
        subchunk2Size = 0;
        decodeInfo = new ArrayList<decodeProporty>();
    }

    public void setDecodeInfo(String chunkID, int chunkSize, String format, String subchunk1ID, int subchunk1Size, int audioFormat, int numChannels, int sampleRate, int byteRate, int blockAlign, int bitsPerSample, String subchunk2ID, int subchunk2Size){
        this.chunkID = chunkID;
        this.chunkSize = chunkSize;
        this.format = format;
        this.subchunk1ID = subchunk1ID;
        this.subchunk1Size = subchunk1Size;
        this.audioFormat = audioFormat;
        this.numChannels = numChannels;
        this.sampleRate = sampleRate;
        this.byteRate = byteRate;
        this.blockAlign = blockAlign;
        this.bitsPerSample = bitsPerSample;
        this.subchunk2ID = subchunk2ID;
        this.subchunk2Size = subchunk2Size;
    }

    public void printDecodeInfo(){
        System.out.println("chunkID: " + chunkID);
        System.out.println("chunkSize: " + chunkSize);
        System.out.println("format: " + format);
        System.out.println("subchunk1ID: " + subchunk1ID);
        System.out.println("subchunk1Size: " + subchunk1Size);
        System.out.println("audioFormat: " + audioFormat);
        System.out.println("numChannels: " + numChannels);
        System.out.println("sampleRate: " + sampleRate);
        System.out.println("byteRate: " + byteRate);
        System.out.println("blockAlign: " + blockAlign);
        System.out.println("bitsPerSample: " + bitsPerSample);
        System.out.println("subchunk2ID: " + subchunk2ID);
        System.out.println("subchunk2Size: " + subchunk2Size);
    }

    public void decodeMusic (String Path){
        try {
            FileInputStream fileInputStream = new FileInputStream(Path);
            byte[] header = new byte[44];
            fileInputStream.read(header);
            fileInputStream.close();

            String chunkID = new String(header, 0, 4, "ASCII");
            int chunkSize = byteArrayToInt(header, 4);
            String format = new String(header, 8, 4, "ASCII");
            String subchunk1ID = new String(header, 12, 4, "ASCII");
            int subchunk1Size = byteArrayToInt(header, 16);
            int audioFormat = byteArrayToShort(header, 20);
            int numChannels = byteArrayToShort(header, 22);
            int sampleRate = byteArrayToInt(header, 24);
            int byteRate = byteArrayToInt(header, 28);
            int blockAlign = byteArrayToShort(header, 32);
            int bitsPerSample = byteArrayToShort(header, 34);
            String subchunk2ID = new String(header, 36, 4, "ASCII");
            int subchunk2Size = byteArrayToInt(header, 40);
            decodeInfo.add(new decodeProporty());
            setDecodeInfo(chunkID, chunkSize, format, subchunk1ID, subchunk1Size, audioFormat, numChannels, sampleRate, byteRate, blockAlign, bitsPerSample, subchunk2ID, subchunk2Size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DecodingPlayback musicPlayer = new DecodingPlayback();
        String Path = "/Users/icerxm/IdeaProjects/csci3280project/src/album/HongKongCooking.wav";
        musicPlayer.decodeMusic(Path);
        musicPlayer.printDecodeInfo();


    }

    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    public static int byteArrayToShort(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 2; i++) {
            int shift = (2 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
}
