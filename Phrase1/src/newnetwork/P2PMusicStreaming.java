package newnetwork;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class P2PMusicStreaming {
    private final ZContext context;

    public P2PMusicStreaming() {
        context = new ZContext();
    }

    public static void main(String[] args) {
        P2PMusicStreaming app = new P2PMusicStreaming();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the server address (e.g., tcp://localhost:5555):");
        String serverAddress = scanner.nextLine();
        System.out.println("Enter your peer address (e.g., tcp://localhost:5556):");
        String bindAddress = scanner.nextLine();

        // Connect to the server and retrieve the list of connected peers
        List<String> peerAddresses = app.connectToServer(serverAddress, bindAddress);

        // Start listening for search, availability, and data requests
        app.startListeners(bindAddress);

        // Send search request and process results
        System.out.println("Enter a search term:");
        String searchTerm = scanner.nextLine();
        List<String> searchResults = app.sendSearchRequest(searchTerm, peerAddresses.get(0));
        System.out.println("Search results:");
        searchResults.forEach(System.out::println);

        // Exit the application
        app.context.close();
    }

    private List<String> connectToServer(String serverAddress, String bindAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.connect(serverAddress);

        // Send the peer's bind address to the server
        socket.send(bindAddress);

        // Receive the list of connected peers from the server
        ZMsg response = ZMsg.recvMsg(socket);
        List<String> peerAddresses = new ArrayList<>();
        response.forEach(frame -> peerAddresses.add(new String(frame.getData())));

        socket.close();
        return peerAddresses;
    }

    private void startListeners(String bindAddress) {
        new Thread(() -> listenForSearchRequests(bindAddress)).start();
        new Thread(() -> listenForAvailabilityRequests(bindAddress)).start();
        new Thread(() -> listenForAudioDataRequests(bindAddress)).start();
    }

    public List<String> sendSearchRequest(String searchTerm, String targetPeerAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.connect(targetPeerAddress);
        socket.send(searchTerm);
        ZMsg response = ZMsg.recvMsg(socket);
        List<String> results = new ArrayList<>();
        response.forEach(frame -> results.add(new String(frame.getData())));
        socket.close();
        return results;
    }

    public void listenForSearchRequests(String bindAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REP);
        socket.bind(bindAddress);

        while (!Thread.currentThread().isInterrupted()) {
            String searchTerm = socket.recvStr();
            // Search local music files (mocked results for simplicity)
            List<String> searchResults = new ArrayList<>();
            searchResults.add("Song1");
            searchResults.add("Song2");

            ZMsg response = new ZMsg();
            searchResults.forEach(response::add);
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
        audioChunks.add(new byte[]{0x01, 0x02, 0x03});
        audioChunks.add(new byte[]{0x04, 0x05, 0x06});
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

// Add methods for sending and receiving image data chunks similar to the audio data methods.
// Then, implement the downloadAndDisplayInterleavedImage method to request different chunks
// of the image from different peers, reassemble the image, and display the interleaved result.
}