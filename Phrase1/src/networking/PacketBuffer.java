package networking;

import java.nio.ByteBuffer;

public class PacketBuffer {
    private static final int CAPACITY_SIZE = 1024;
    private static final int MAX_CAPACITY_SIZE = 1024 * 1024 * 512; // hard clip at 512MiB, though we should not hit this limit
    private ByteBuffer backendBuffer;

    public PacketBuffer(){
        this.backendBuffer = ByteBuffer.allocate(CAPACITY_SIZE);
    }

    public PacketBuffer(ByteBuffer backendBuffer){
        this.backendBuffer = backendBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return backendBuffer;
    }

    public int getInt(){
        return backendBuffer.getInt();
    }
    public byte get(){
        return backendBuffer.get();
    }
    public void get(byte[] buf, int offset, int len){
        backendBuffer.get(buf, offset, len);
    }
    public short getShort(){
        return backendBuffer.getShort();
    }
    public long getLong(){
        return backendBuffer.getLong();
    }
    public char getChar(){
        return backendBuffer.getChar();
    }
    public float getFloat(){
        return backendBuffer.getFloat();
    }
    public double getDouble(){
        return backendBuffer.getDouble();
    }
    public String getString(){
        int stringLength = backendBuffer.getInt();
        char[] charArray = new char[stringLength];
        for (int i = 0; i < stringLength; i++) {
            charArray[i] = backendBuffer.getChar();
        }
        return new String(charArray);
    }

    private void requestLargerBuffer(int requiredLength){
        int currentSize = backendBuffer.capacity();
        if (currentSize >= MAX_CAPACITY_SIZE){
            throw new IllegalStateException("hit max capacity size");
        }
        int newSize = Math.min(currentSize * 2, MAX_CAPACITY_SIZE);
        while (newSize < currentSize + requiredLength - backendBuffer.remaining()){
            if (newSize >= MAX_CAPACITY_SIZE){
                throw new IllegalStateException("hit max capacity size");
            }
            newSize = Math.min(newSize * 2, MAX_CAPACITY_SIZE);
        }
        ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
        backendBuffer.flip();
        newBuffer.put(backendBuffer);
        backendBuffer = newBuffer;
    }

    public PacketBuffer putInt(int v){
        if(backendBuffer.remaining() < Integer.BYTES){
            requestLargerBuffer(Integer.BYTES);
        }
        backendBuffer.putInt(v);
        return this;
    }

    public PacketBuffer put(byte v){
        if(backendBuffer.remaining() < Byte.BYTES){
            requestLargerBuffer(Byte.BYTES);
        }
        backendBuffer.put(v);
        return this;
    }

    public PacketBuffer put(byte[] v, int offset, int len){
        var requestLength = Byte.BYTES * (len - offset);
        if(backendBuffer.remaining() < requestLength){
            requestLargerBuffer(requestLength);
        }
        backendBuffer.put(v, offset, len);
        return this;
    }

    public PacketBuffer putShort(short v){
        if(backendBuffer.remaining() < Short.BYTES){
            requestLargerBuffer(Short.BYTES);
        }
        backendBuffer.putShort(v);
        return this;
    }

    public PacketBuffer putLong(long v){
        if(backendBuffer.remaining() < Long.BYTES){
            requestLargerBuffer(Long.BYTES);
        }
        backendBuffer.putLong(v);
        return this;
    }

    public PacketBuffer putChar(char v){
        if(backendBuffer.remaining() < Character.BYTES){
            requestLargerBuffer(Character.BYTES);
        }
        backendBuffer.putChar(v);
        return this;
    }
    public PacketBuffer putFloat(float v){
        if(backendBuffer.remaining() < Float.BYTES){
            requestLargerBuffer(Float.BYTES);
        }
        backendBuffer.putFloat(v);
        return this;
    }

    public PacketBuffer getDouble(double v){
        if(backendBuffer.remaining() < Double.BYTES){
            requestLargerBuffer(Double.BYTES);
        }
        backendBuffer.putDouble(v);
        return this;
    }

    public PacketBuffer putString(String string){
        var requiredLength = Integer.BYTES + Character.BYTES * string.length();
        if(backendBuffer.remaining() < requiredLength){
            requestLargerBuffer(requiredLength);
        }
        backendBuffer.putInt(string.length());
        for (char c : string.toCharArray()) {
            backendBuffer.putChar(c);
        }
        return this;
    }
}
