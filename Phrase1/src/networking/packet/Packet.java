package networking.packet;

import networking.PacketBuffer;

public abstract class Packet {
    public abstract void read(PacketBuffer packetBuffer);

    public abstract void write(PacketBuffer packetBuffer);
}
