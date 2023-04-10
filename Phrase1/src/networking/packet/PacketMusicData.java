package networking.packet;

import networking.PacketBuffer;

/**
 * response packet for {@code PacketRequestMusicData}, providing requested music data
 */
public class PacketMusicData extends Packet{

    /** the path of requested music **/
    private String requestMusic;
    /** offset of sent data to the music **/
    private int pos;
    /** length of sent data **/
    private int len;
    private byte[] data;

    public PacketMusicData() {
    }

    public PacketMusicData(String requestMusic, int pos, int len, byte[] data){
        this.requestMusic = requestMusic;
        this.pos = pos;
        this.len = len;
        this.data = data;
    }

    @Override
    public void read(PacketBuffer packetBuffer) {
        requestMusic = packetBuffer.getString();
        pos = packetBuffer.getInt();
        len = packetBuffer.getInt();
        data = new byte[len];
        packetBuffer.get(data, 0, len);
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        packetBuffer.putString(requestMusic);
        packetBuffer.putInt(pos);
        packetBuffer.putInt(len);
        packetBuffer.put(data, 0, len);
    }
}
