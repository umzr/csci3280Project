package newnetwork;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrackerServer {
    private final ZContext context;
    private final Set<String> onlinePeers;

    public TrackerServer() {
        context = new ZContext();
        onlinePeers = new HashSet<>();
    }

    public void start(String bindAddress) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REP);
        socket.bind(bindAddress);

        while (!Thread.currentThread().isInterrupted()) {
            String request = socket.recvStr();
            String[] parts = request.split(":", 2);
            String command = parts[0];

            System.out.println("Received command: " + request);
            
            if ("REGISTER".equals(command)) {
                String peerAddress = parts[1];
                onlinePeers.add(peerAddress);
                socket.send("OK");
            } else if ("UNREGISTER".equals(command)) {
                String peerAddress = parts[1];
                onlinePeers.remove(peerAddress);
                socket.send("OK");
            } else if ("GET_PEERS".equals(command)) {
                String peerList = String.join(",", onlinePeers);
                socket.send(peerList);
            } else {
                socket.send("ERROR:INVALID_COMMAND");
            }
        }

        socket.close();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the tracker server address (e.g., tcp://localhost:4444):");
        String trackerAddress = scanner.nextLine();
        if(trackerAddress == null || trackerAddress.isBlank()){
            trackerAddress = "tcp://localhost:4444";
        }
        System.out.println("Server start at: "+trackerAddress);

        TrackerServer trackerServer = new TrackerServer();
        trackerServer.start(trackerAddress);
    }
}


