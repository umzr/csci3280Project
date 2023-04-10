package networking.packet;

import networking.PacketBuffer;

/**
 * response packet for {@code PacketRequestMusicData}, saying that we cannot provide music data requested
 */
public class PacketMusicNotAvailable extends Packet{
    private String requestMusic;

    public PacketMusicNotAvailable(){

    }
    public PacketMusicNotAvailable(String music){
        this.requestMusic = music;
    }

    public String getRequestMusic() {
        return requestMusic;
    }

    @Override
    public void read(PacketBuffer packetBuffer) {
        requestMusic = packetBuffer.getString();
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        packetBuffer.putString(requestMusic);
    }
}
