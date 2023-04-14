package newnetwork;

import music.MusicProperty;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static music.MusicProperty.decodeMusicProperty;
import static music.MusicProperty.flat2byteMusicProperty;

public class P2PMusicStreaming {
    private final ZContext context;

    public P2PMusicStreaming() {
        context = new ZContext();
    }

    private  ArrayList<MusicProperty> musicManager;

    public void setMusicManager(ArrayList<MusicProperty> musicManager) {
        this.musicManager = musicManager;
    }

    public ArrayList<MusicProperty> getMusicManager() {
        return musicManager;
    }


    public static P2PMusicStreaming run(String trackerAddress, String bindAddress) {
        P2PMusicStreaming app = new P2PMusicStreaming();
        app.registerWithTracker(trackerAddress, bindAddress);
        new Thread(() -> app.initStartListener(bindAddress)).start();
        List<String> onlinePeers = app.getOnlinePeers(trackerAddress);
        while (onlinePeers.isEmpty()) {
            System.out.println("No peers online. Waiting for peers to connect...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onlinePeers = app.getOnlinePeers(trackerAddress);
        }
        for(String peer : onlinePeers) {
            System.out.println(peer);
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
        app.registerWithTracker(trackerAddress, bindAddress);

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
        app.unregisterWithTracker(trackerAddress, bindAddress);
        app.context.close();
    }

    public void registerWithTracker(String trackerAddress, String peerAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.connect(trackerAddress);
        socket.send("REGISTER:" + peerAddress);
        String response = socket.recvStr();
        if ("OK".equals(response)) {
            System.out.println("Registered with tracker successfully.");
        } else {
            System.err.println("Failed to register with tracker.");
        }
        socket.close();
    }

    public void unregisterWithTracker(String trackerAddress, String peerAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.connect(trackerAddress);
        socket.send("UNREGISTER:" + peerAddress);
        String response = socket.recvStr();
        if ("OK".equals(response)) {
            System.out.println("Unregistered from tracker successfully.");
        } else {
            System.err.println("Failed to unregister from tracker.");
        }
        socket.close();
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
            switch (recvArr[0]){
                case "SEARCH":
                    String searchTerm = recvArr[1];
                    System.out.println("Received search request for " + searchTerm);
                    ArrayList<MusicProperty> musicInfo = getMusicManager();

                    byte[] send = flat2byteMusicProperty(musicInfo);

                    ZMsg response = new ZMsg();
                    response.add(send);
                    response.send(socket);

                    break;
                case "AVAILABILITY":
                    break;
                default:
                    System.out.println("Invalid request");
                    break;
            }

        }

        socket.close();


    }
    private void startListeners(String bindAddress) {
        new Thread(() -> listenForSearchRequests(bindAddress)).start();
        new Thread(() -> listenForAvailabilityRequests(bindAddress)).start();
        new Thread(() -> listenForAudioDataRequests(bindAddress)).start();
    }

    public ArrayList<MusicProperty> sendSearchRequest(String searchTerm, String targetPeerAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);

        List<String> target  = getOnlinePeers("tcp://localhost:4444");
        socket.connect(targetPeerAddress);
        socket.send("SEARCH|" + searchTerm);
        ZMsg response = ZMsg.recvMsg(socket);

        byte[] recv = response.getFirst().getData();
        ArrayList<MusicProperty> musicInfo = decodeMusicProperty(recv);
        socket.close();
        return musicInfo;
    }



    public void listenForSearchRequests(String bindAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REP);
        socket.bind(bindAddress);
        System.out.println("Listening for search requests on " + bindAddress);
        while (!Thread.currentThread().isInterrupted()) {
            String searchTerm = socket.recvStr();
            System.out.println("Received search request for " + searchTerm);
            ArrayList<MusicProperty> musicInfo = getMusicManager();

            byte[] send = flat2byteMusicProperty(musicInfo);

            ZMsg response = new ZMsg();
            response.add(send);
            response.send(socket);
        }

        socket.close();
    }

    public void listenForAvailabilityRequests(String bindAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REP);
        socket.bind(bindAddress);

        while (!Thread.currentThread().isInterrupted()) {
            String fileName = socket.recvStr();
            // Check if the file exists locally (mocked response for simplicity)
            boolean fileExists = true;
            socket.send(fileExists ? "1" : "0");
        }

        socket.close();
    }

    public void listenForAudioDataRequests(String bindAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REP);
        socket.bind(bindAddress);

        while (!Thread.currentThread().isInterrupted()) {
            String fileName = socket.recvStr();
            // Send the requested audio data chunks (omitted

            // Retrieve audio file chunks and send them
            List<byte[]> audioChunks = getAudioChunks(fileName);
            ZMsg audioData = new ZMsg();
            for (byte[] chunk : audioChunks) {
                audioData.add(chunk);
            }
            audioData.send(socket);
        }

        socket.close();
    }

    private List<byte[]> getAudioChunks(String fileName) {
        // Load and divide the audio file into smaller chunks
        // For simplicity, we return a mocked list of byte arrays
        List<byte[]> audioChunks = new ArrayList<>();
        audioChunks.add(new byte[] { 0x01, 0x02, 0x03 });
        audioChunks.add(new byte[] { 0x04, 0x05, 0x06 });
        return audioChunks;
    }

    public void playAudioInterleaved(String fileName, List<String> peerAddresses) {
        // Request different chunks of the audio file from different peers
        // For simplicity, we assume only two peers and two chunks
        byte[] chunk1 = requestAudioChunk(peerAddresses.get(0), fileName, 1);
        byte[] chunk2 = requestAudioChunk(peerAddresses.get(1), fileName, 2);

        // Combine the chunks and play the audio (omitted for simplicity)
    }

    private byte[] requestAudioChunk(String targetPeerAddress, String fileName, int chunkIndex) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.connect(targetPeerAddress);
        socket.send(fileName + ":" + chunkIndex);
        byte[] chunkData = socket.recv();
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