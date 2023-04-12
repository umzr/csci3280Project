package newnetwork;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;

public class Server {
    private final ZContext context;
    private List<String> connectedPeers;

    public Server() {
        context = new ZContext();
        connectedPeers = new ArrayList<>();
    }

    public void start(String bindAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REP);
        socket.bind(bindAddress);

        while (!Thread.currentThread().isInterrupted()) {
            // Receive a new peer's bind address
            String peerAddress = socket.recvStr();

            // Add the new peer to the list of connected peers
            connectedPeers.add(peerAddress);
            System.out.println("New peer connected: " + peerAddress);

            // Send the list of connected peers to the new peer
            ZMsg peerList = new ZMsg();
            for (String address : connectedPeers) {
                peerList.add(address);
            }
            peerList.send(socket);
        }

        socket.close();
    }
    public static void main(String[] args) {
        Server server = new Server();
        String bindAddress = "tcp://*:5555"; // Replace with your desired bind address
        server.start(bindAddress);
    }
}



