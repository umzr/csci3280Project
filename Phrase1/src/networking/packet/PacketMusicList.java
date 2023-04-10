package networking.packet;

import music.MusicManager;
import music.MusicProperty;
import networking.PacketBuffer;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Packet for sending available music of this machine to another machine
 */
public class PacketMusicList extends Packet{

    private ArrayList<MusicProperty> musicInfo;

    public PacketMusicList(){

    }

    public PacketMusicList(ArrayList<MusicProperty> musicInfo){
        this.musicInfo = musicInfo;
    }

    public ArrayList<MusicProperty> getMusicInfo() {
        return musicInfo;
    }

    @Override
    public void read(PacketBuffer packetBuffer) {
        String csv = packetBuffer.getString();
        StringReader reader = new StringReader(csv);
        this.musicInfo = MusicManager.loadFromCsv(reader);
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        StringWriter writer = new StringWriter();
        MusicManager.saveMusicProperty(this.musicInfo, writer);
        packetBuffer.putString(writer.toString());
    }
}
