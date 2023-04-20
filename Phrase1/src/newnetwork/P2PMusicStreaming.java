package newnetwork;

import music.MusicManager;
import music.MusicProperty;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static music.MusicProperty.decodeMusicProperty;
import static music.MusicProperty.flat2byteMusicProperty;

public class P2PMusicStreaming {
    public static final String AVAILABILITY = "AVAILABILITY";
    public static final String AUDIOCHUNK = "AUDIOCHUNK";
    private final ZContext context;
    private String trackerAddress;

    public P2PMusicStreaming() {
        context = new ZContext();
    }

    private  MusicManager musicManager;

    public void setMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public String ClientAddress = null;
    public String getClientAddress() {
        return ClientAddress;
    }
    public void setClientAddress(String clientAddress) {
        ClientAddress = clientAddress;
    }
    public String getTrackerAddress(){
        if(trackerAddress != null){
            return trackerAddress;
        }
        return "tcp://localhost:4444";
    }

    public static P2PMusicStreaming run(String trackerAddress, String bindAddress) {
        P2PMusicStreaming app = new P2PMusicStreaming();
        app.setClientAddress(bindAddress);
        app.registerWithTracker(trackerAddress);
        new Thread(() -> app.initStartListener(bindAddress)).start();
        List<String> onlinePeers = app.getOnlinePeers(trackerAddress);
        if(onlinePeers.isEmpty()){
            System.out.println("No peers online.");
        }
        else{
            System.out.println("Other online peers: " + String.join(", ", onlinePeers));
        }
        return app;
    }

    public static void main(String[] args) {
        P2PMusicStreaming app = new P2PMusicStreaming();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the tracker server address (e.g., tcp://localhost:4444):");
        String trackerAddress = scanner.nextLine();
        System.out.println("Enter your peer address (e.g., tcp://localhost:5555):");
        String bindAddress = scanner.nextLine();

        // Register with the tracker server
        app.setClientAddress(bindAddress);
        app.registerWithTracker(trackerAddress);

        // Start listeners for search, availability, and data requests
//        app.startListeners(bindAddress);

        // Get the list of online peers from the tracker server
        List<String> onlinePeers = app.getOnlinePeers(trackerAddress);

        // Send search requests and process results
        while (onlinePeers.isEmpty()) {
            System.out.println("No peers online. Waiting for peers to connect...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onlinePeers = app.getOnlinePeers(trackerAddress);
        }

        for (String peer : onlinePeers) {
            System.out.println(peer);
        }

        System.out.println("------------------");
        Boolean FLAG = true;
        while (FLAG) {
            onlinePeers = app.getOnlinePeers(trackerAddress);
            System.out.println("*************");
            System.out.println("Online peers:");
            for (String peer : onlinePeers) {
                System.out.println(peer);
            }
            System.out.println("*************");

            System.out.println("Enter a search term:");
            String searchTerm = scanner.nextLine();

            System.out.println("onlinePeers.get(0): " + onlinePeers.get(0));
            List<String> searchResults = new ArrayList<>();
            for (String peer : onlinePeers) {
                System.out.println("peer" + peer);
//                List<String> temp = app.sendSearchRequest(searchTerm, peer);
//                searchResults.addAll(temp);

//                System.out.println("searchResults: " + temp);

                System.out.println("-------------");
            }

            System.out.println("Search results:");
            searchResults.forEach(System.out::println);
            System.out.println("Do you want to continue? (Y/N)");
            String answer = scanner.nextLine();
            if (answer.equals("N")) {
                FLAG = false;
            }
        }

        // Unregister from the tracker server and exit the application
        app.unregisterWithTracker();
        app.context.close();
    }

    public void registerWithTracker(String trackerAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.setSendTimeOut(10000);
        socket.setReceiveTimeOut(10000);
        socket.connect(trackerAddress);
        if(!socket.send("REGISTER:" + this.getClientAddress())){
            socket.close();
            throw new RuntimeException(String.format("Connection error: %s", ZMQ.Error.findByCode(socket.errno()).getMessage()));
        }
        String response = socket.recvStr();
        if(response == null){
            socket.close();
            throw new RuntimeException(String.format("Connection error: %s", ZMQ.Error.findByCode(socket.errno()).getMessage()));
        }
        if ("OK".equals(response)) {
            this.trackerAddress = trackerAddress;
            System.out.println("Registered with tracker successfully.");
        } else {
            System.err.println("Failed to register with tracker.");
        }
        socket.close();
    }

    public void unregisterWithTracker() {
        if(this.trackerAddress != null){
            ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
            socket.connect(this.getTrackerAddress());
            socket.send("UNREGISTER:" + this.getClientAddress());
            String response = socket.recvStr();
            if ("OK".equals(response)) {
                System.out.println("Unregistered from tracker successfully.");
            } else {
                System.err.println("Failed to unregister from tracker.");
            }
            socket.close();
            this.trackerAddress = null;
        }
    }

    public void close(){
        unregisterWithTracker();
        context.close();
    }

    public List<String> getOnlinePeers(String trackerAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.connect(trackerAddress);
        socket.send("GET_PEERS");
        String peerList = socket.recvStr();
        socket.close();

        List<String> peers = new ArrayList<>();
        if (peerList != null && !peerList.isEmpty()) {
            for (String address : peerList.split(",")) {
                if(address.equals(this.getClientAddress())) continue;
                System.out.println("There are peers online: " + address);
                peers.add(address.trim());
            }
        }
        return peers;
    }


    private void initStartListener (String bindAddress){

        ZMQ.Socket socket = context.createSocket(ZMQ.REP);
        socket.bind(bindAddress);
        System.out.println("Listening for search requests on " + bindAddress);
        while (!Thread.currentThread().isInterrupted()) {
            String recv = socket.recvStr();
            // recv --> action | <some args>

            String[] recvArr = recv.split("\\|");
            ZMsg response;
            switch (recvArr[0]){
                case "SEARCH":
                    String searchTerm = recvArr[1];
                    System.out.println("Received search request for " + searchTerm);
                    ArrayList<MusicProperty> musicInfo = getMusicManager().getLocalMusicInfo();
                    String clinetAddress = getClientAddress();
                    byte[] send = flat2byteMusicProperty(musicInfo, clinetAddress);

                    response = new ZMsg();
                    response.add(send);
                    response.send(socket);

                    break;
                case AVAILABILITY:
                    String filepath = recvArr[1];
                    System.out.println("Received availability check for " + filepath);
                    response = new ZMsg();
                    boolean found = musicManager.isPathLocalMusic(filepath);
                    response.add(new byte[]{(byte) (found ? 1 : 0)});
                    response.send(socket);
                    break;
                case AUDIOCHUNK:
                    String request = recvArr[1];
                    String[] splitted = request.split(":");
                    String fp = splitted[0];
                    String chunkStr = splitted[1];
                    int chunk = Integer.parseInt(chunkStr);
                    System.out.println("Received audio chunk request for " + request);
                    response = new ZMsg();
                    String chunkData = getAudioChunks(fp, chunk);
                    response.add(chunkData);
                    response.send(socket);
                    break;
                default:
                    System.out.println("Invalid request");
                    break;
            }

        }

        socket.close();


    }

    public ArrayList<MusicProperty> sendSearchRequest(String searchTerm, String targetPeerAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);

        socket.connect(targetPeerAddress);
        socket.send("SEARCH|" + searchTerm);
        ZMsg response = ZMsg.recvMsg(socket);

        byte[] recv = response.getFirst().getData();
        ArrayList<MusicProperty> musicInfo = decodeMusicProperty(recv);
        socket.close();
        return musicInfo;
    }

    public boolean askForAvailability(String filepath, String targetPeerAddress){
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);

        socket.connect(targetPeerAddress);
        socket.send(AVAILABILITY + "|" + filepath);
        ZMsg response = ZMsg.recvMsg(socket);
        byte[] recv = response.getFirst().getData();
        boolean available = recv[0] == 1;
        socket.close();
        return available;
    }

    private byte[] compress(byte[] data){
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(data);
        deflater.finish();
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            byte[] subchunks = new byte[1024];
            while (!deflater.finished()){
                int readed = deflater.deflate(subchunks);
                if(readed > 0) bos.write(subchunks, 0, readed);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            return data;
        }
    }

    private byte[] decompress(byte[] compressedData){
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            byte[] subchunks = new byte[1024];
            while (!inflater.finished()){
                int readed = inflater.inflate(subchunks);
                if(readed > 0) bos.write(subchunks, 0, readed);
            }
            return bos.toByteArray();
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
            return compressedData;
        }
    }

    private String getAudioChunks(String fileName, int chunk) {
        // Load and divide the audio file into smaller chunks
        try(AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName))) {
            ais.skip((long) (chunk * ais.getFormat().getFrameSize() * ais.getFormat().getSampleRate()));
            byte[] data = new byte[Math.toIntExact((long) (ais.getFormat().getFrameSize() * ais.getFormat().getSampleRate()))];
            int readed = ais.read(data);
            if(readed < 0) return "0";
            if(readed != data.length){
                byte[] tmp = data;
                data = new byte[readed];
                System.arraycopy(tmp, 0, data, 0, readed);
            }
            byte[] compressed = compress(data);
            return Base64.getEncoder().encodeToString(compressed);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public byte[] requestAudioChunk(String targetPeerAddress, String fileName, int chunkIndex) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.connect(targetPeerAddress);
        socket.send(AUDIOCHUNK + "|" + fileName + ":" + chunkIndex);
        var recvStr = socket.recvStr();
        byte[] chunkData;
        if(recvStr.equals("0")){
            // a single '0' string must not be in base64. It is safe to indicate receiving nothing
            chunkData = new byte[0];
        }
        else{
            chunkData = Base64.getDecoder().decode(recvStr);
            chunkData = decompress(chunkData);
        }
        socket.close();
        return chunkData;
    }

    // Add methods for sending and receiving image data chunks similar to the audio
    // data methods.
    // Then, implement the downloadAndDisplayInterleavedImage method to request
    // different chunks
    // of the image from different peers, reassemble the image, and display the
    // interleaved result.
}