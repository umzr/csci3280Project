package networking;

import networking.packet.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NetworkClient {
    private final SocketChannel socketChannel;
    private final Selector selector;
    private final Session session;
    private Thread thread;

    private NetworkClient(SocketChannel socketChannel, Selector selector, Session session) {
        this.socketChannel = socketChannel;
        this.selector = selector;
        this.session = session;
    }

    public static NetworkClient connect(String ip){
        return connect(ip, NetworkServer.DEFAULT_PORT);
    }

    public static NetworkClient connect(String ip, int port){
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            Session session = new Session(socketChannel, selector);
            if(socketChannel.connect(new InetSocketAddress(ip, port))){
                // immediate connection. Start reading
                socketChannel.register(selector, SelectionKey.OP_READ, session);
            }
            else{
                // wait for connection to complete
                socketChannel.register(selector, SelectionKey.OP_CONNECT, session);
            }
            NetworkClient client = new NetworkClient(socketChannel, selector, session);
            client.start();
            return client;
        } catch (IOException e) {
            System.out.printf("Failed to connect to server at %s:%d%n", ip, port);
            e.printStackTrace();
            return null;
        }
    }

    public Session getSession() {
        return session;
    }

    public void sendMessage(Packet packet){
        this.session.sendMessage(packet);
    }

    public void start(){
        this.thread = new Thread(this::networkLoop);
        this.thread.start();
    }

    private void networkLoop(){
        while(this.socketChannel != null && this.socketChannel.isOpen()){
            try {
                if(selector.select() == 0) continue;

                for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if(!key.isValid()) continue;
                    if(key.isConnectable()){
                        SocketChannel channel = (SocketChannel) key.channel();
                        try{
                            channel.finishConnect();
                            key.interestOps(SelectionKey.OP_READ);
                        } catch (IOException e) {
                            System.out.printf("Disconnected: %s%n", e.getMessage());
                            key.cancel();
                            return;
                        }
                    }
                    else if(key.isValid() && key.isReadable()){
                        Session session = (Session) key.attachment();
                        session.handleRead(key);
                    }
                    else if(key.isValid() && key.isWritable()){
                        Session session = (Session) key.attachment();
                        session.handleWrite(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void disconnect(){
        if(this.socketChannel != null && this.socketChannel.isOpen()){
            try {
                this.socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
