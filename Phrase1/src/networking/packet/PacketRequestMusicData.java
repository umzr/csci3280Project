package networking.packet;

import networking.PacketBuffer;

/**
 * Packet to request music data on other machines
 */
public class PacketRequestMusicData extends Packet{

    private String requestMusic;
    private int pos;
    private int len;

    public PacketRequestMusicData(){

    }

    public PacketRequestMusicData(String requestMusic, int pos, int len){
        this.requestMusic = requestMusic;
        this.pos = pos;
        this.len = len;
    }

    public String getRequestMusic() {
        return requestMusic;
    }

    public int getLen() {
        return len;
    }

    public int getPos() {
        return pos;
    }

    @Override
    public void read(PacketBuffer packetBuffer) {
        requestMusic = packetBuffer.getString();
        pos = packetBuffer.getInt();
        len = packetBuffer.getInt();
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        packetBuffer.putString(requestMusic);
        packetBuffer.putInt(pos);
        packetBuffer.putInt(len);
    }
}
