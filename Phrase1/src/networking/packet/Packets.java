package networking.packet;

import java.util.HashMap;
import java.util.function.Supplier;

public class Packets {
    private Packets(){}

    private static final HashMap<Class<? extends Packet>, RegistryInfo> REGISTERED_PACKETS = new HashMap<>();
    private static final HashMap<Integer, RegistryInfo> REGISTERED_PACKETS_ID = new HashMap<>();

    static {
        register(PacketMusicList.class, PacketMusicList::new);
        register(PacketRequestMusicData.class, PacketRequestMusicData::new);
        register(PacketMusicNotAvailable.class, PacketMusicNotAvailable::new);
        register(PacketMusicData.class, PacketMusicData::new);
    }

    private static <T extends Packet> void register(Class<T> clazz, Supplier<T> supplier){
        int id = REGISTERED_PACKETS.size();
        REGISTERED_PACKETS.put(clazz, new RegistryInfo(id, clazz, supplier));
        REGISTERED_PACKETS_ID.put(id, new RegistryInfo(id, clazz, supplier));
    }

    public static <T extends Packet> int getId(Class<T> clazz){
        return REGISTERED_PACKETS.get(clazz).getId();
    }

    public static <T extends Packet> Class<T> getClass(int id){
        return REGISTERED_PACKETS_ID.get(id).getPacketClass();
    }

    public static <T extends Packet> T create(int id){
        return (T) REGISTERED_PACKETS_ID.get(id).getSupplier().get();
    }

    public static <T extends Packet> T create(Class<T> clazz){
        return (T) REGISTERED_PACKETS.get(clazz).getSupplier().get();
    }

    public static class RegistryInfo<T extends Packet>{
        private final int id;
        private final Class<T> clazz;
        private final Supplier<T> supplier;

        private RegistryInfo(int id, Class<T> clazz, Supplier<T> supplier){
            this.id = id;
            this.clazz = clazz;
            this.supplier = supplier;
        }

        public int getId() {
            return id;
        }

        public Class<T> getPacketClass() {
            return clazz;
        }

        public Supplier<T> getSupplier() {
            return supplier;
        }
    }
}
