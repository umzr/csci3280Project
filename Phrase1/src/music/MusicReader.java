package music;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class MusicReader {

    public static AudioFormat getAudioFormat(String fpath){
        try(FileInputStream stream = new FileInputStream(fpath)){
            byte[] bytes = stream.readNBytes(4);
            if(new String(bytes, 0, 4).equals("RIFF")){
                bytes = stream.readNBytes(4);
                int fullFileSize = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                bytes = stream.readNBytes(4);
                String fileFormatName = new String(bytes, 0, 4);
                if(fileFormatName.equals("WAVE")){
                    short audioFormat = 0;
                    short channel = 0;
                    int samplingRate = 0;
                    int byteRate = 0;
                    short bitPerSample = 0;
                    AudioFormat.Encoding encoding = null;
                    HashMap<String, Object> props = new HashMap<>();
                    while(true){
                        bytes = stream.readNBytes(4);
                        if (bytes.length < 4) break;
                        String subchunkTag = new String(bytes, 0, 4);
                        bytes = stream.readNBytes(4);
                        if (bytes.length < 4) break;
                        int size =  ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        if(subchunkTag.startsWith("fmt")){
                            bytes = stream.readNBytes(size);
                            audioFormat = ByteBuffer.wrap(bytes, 0, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
                            channel = ByteBuffer.wrap(bytes, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
                            samplingRate = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                            byteRate = ByteBuffer.wrap(bytes, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                            bitPerSample = ByteBuffer.wrap(bytes, 14, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
                            if (audioFormat == 3) {
                                encoding = AudioFormat.Encoding.PCM_FLOAT;
                            } else if (audioFormat == 6) {
                                encoding = AudioFormat.Encoding.ALAW;
                            } else if (audioFormat == 7) {
                                encoding = AudioFormat.Encoding.ULAW;
                            } else {
                                encoding = bitPerSample / 8 == 1 ? AudioFormat.Encoding.PCM_UNSIGNED : AudioFormat.Encoding.PCM_SIGNED;
                            }
                        }
                        else if(subchunkTag.equals("LIST")){
                            bytes = stream.readNBytes(4);
                            String listType = new String(bytes, 0, 4);
                            int remainingSize = size - bytes.length;
                            if(listType.equals("INFO")){
                                while(remainingSize > 0){
                                    bytes = stream.readNBytes(4);
                                    String propName = new String(bytes, 0, 4);
                                    remainingSize -= bytes.length;
                                    bytes = stream.readNBytes(4);
                                    int stringLength = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                    remainingSize -= bytes.length;
                                    bytes = stream.readNBytes(stringLength);
                                    String propValue = new String(bytes);
                                    remainingSize -= bytes.length;
                                    switch (propName) {
                                        case "IART":
                                            props.put("author", propValue);
                                            break;
                                        case "ICRD":
                                            props.put("created", propValue);
                                            break;
                                        case "ITRK":
                                        case "TRCK":
                                            props.put("track", propValue);
                                            break;
                                        case "INAM":
                                        case "TITL":
                                            props.put("title", propValue);
                                            break;
                                        case "IGNR":
                                        case "GENR":
                                            props.put("genre", propValue);
                                            break;
                                        case "YEAR":
                                            props.put("year", propValue);
                                            break;
                                        case "CMNT":
                                        case "COMM":
                                        case "ICMT":
                                            props.put("comment", propValue);
                                            break;
                                    }
                                }
                            }
                            else{
                                stream.skip(remainingSize);
                            }
                        }
                        else{
                            stream.skip(size);
                        }
                    }
                    if(encoding != null){
                        return new AudioFormat(encoding, samplingRate, bitPerSample, channel, (bitPerSample + 7) / 8 * channel, samplingRate, false, props);
                    }
                }
            }
            // if we reach here, the file should not be WAVE file. fallback to java-provided method
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(fpath))){
            return inputStream.getFormat();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
