package networking;

import networking.packet.Packet;
import networking.packet.Packets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Session {
    public static final int BUFFER_SIZE = 1024;
    private final SocketChannel clientChannel;
    private final Selector selector;

    public Session(SocketChannel clientChannel, Selector selector) {
        this.clientChannel = clientChannel;
        this.selector = selector;
    }

    private Queue<ByteBuffer> pendingOutgoingBuffer = new ArrayDeque<>();

    public void handleWrite(SelectionKey key) throws IOException {
        int writtenBytes = 0;
        int totalWrittenBytes = 0;
        ByteBuffer buffer = pendingOutgoingBuffer.peek();
        if (buffer == null){
            // no more message to be sent
            key.interestOps(SelectionKey.OP_READ);
            return;
        }
        writtenBytes = this.clientChannel.write(buffer);
        totalWrittenBytes = writtenBytes;
        while (writtenBytes > 0){
            if(!buffer.hasRemaining()){
                pendingOutgoingBuffer.poll();
                buffer = pendingOutgoingBuffer.peek();
                if(buffer == null){
                    // no more message to be sent
                    key.interestOps(SelectionKey.OP_READ);
                    return;
                }
            }
            writtenBytes = this.clientChannel.write(buffer);
            totalWrittenBytes += writtenBytes;
        }
        if(!buffer.hasRemaining()){
            pendingOutgoingBuffer.poll();
        }
    }

    private ByteBuffer pendingIncomingBuffer = null;

    public void handleRead(SelectionKey key) throws IOException {
        Queue<ByteBuffer> buffers = new ArrayDeque<>();
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
        int readBytes = this.clientChannel.read(buf);
        int totalReadBytes = readBytes;
        while(readBytes > 0){
            if (!buf.hasRemaining()){
                buffers.add(buf);
                buf = ByteBuffer.allocate(BUFFER_SIZE);
            }
            readBytes = this.clientChannel.read(buf);
            totalReadBytes += readBytes;
        }
        if(buf.position() > 0){
            // add nonempty nonfull buffer into queue too
            buffers.add(buf);
        }
        if(readBytes == -1){
            // receiving -1 here means that the stream os reach the end. we can disconnect now
            //TODO disconnection
            clientChannel.close();
        }
        int remainingPacketLength = 0;
        if(pendingIncomingBuffer != null){
            remainingPacketLength = pendingIncomingBuffer.remaining();
        }
        ArrayList<PacketBuffer> packetBuffers = new ArrayList<>();
        for (ByteBuffer buffer : buffers) {
            buffer.flip(); // flip the buffer so that we can read it
            while(buffer.hasRemaining()){
                if (remainingPacketLength == 0){
                    if(pendingIncomingBuffer != null){
                        pendingIncomingBuffer.flip();
                        packetBuffers.add(new PacketBuffer(pendingIncomingBuffer));
                    }
                    remainingPacketLength = buffer.getInt();
                    pendingIncomingBuffer = ByteBuffer.allocate(remainingPacketLength);
                }
                byte[] rawBuf = new byte[Math.min(remainingPacketLength, buffer.remaining())];
                buffer.get(rawBuf);
                pendingIncomingBuffer.put(rawBuf);
            }
        }
        if (remainingPacketLength == 0 && pendingIncomingBuffer != null){
            pendingIncomingBuffer.flip();
            packetBuffers.add(new PacketBuffer(pendingIncomingBuffer));
        }
        for (PacketBuffer buffer : packetBuffers) {
            int packetId = buffer.getInt();
            Packet packet = Packets.create(packetId);
            packet.read(buffer);
            //TODO handle packet
        }
    }

    public Selector getSelector() {
        return selector;
    }

    public void sendMessage(Packet packet) {
        PacketBuffer packetBuffer = new PacketBuffer();
        packet.write(packetBuffer);
        packetBuffer.getByteBuffer().flip();
        int limit = packetBuffer.getByteBuffer().limit();
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES * 2 + limit);
        buf.putInt(limit + Integer.BYTES);
        buf.putInt(Packets.getId(packet.getClass()));
        buf.put(packetBuffer.getByteBuffer());
        buf.flip();
        pendingOutgoingBuffer.add(buf);
        clientChannel.keyFor(selector).interestOpsOr(SelectionKey.OP_WRITE);
        selector.wakeup();
    }
}
