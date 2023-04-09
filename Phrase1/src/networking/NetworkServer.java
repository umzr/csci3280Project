package networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class NetworkServer {
    public static final int DEFAULT_PORT = 30000;
    private final ServerSocketChannel channel;
    private final Selector selector;
    private Thread thread;

    private NetworkServer(ServerSocketChannel channel, Selector selector){
        this.channel = channel;
        this.selector = selector;
    }

    public static NetworkServer openServer(){
        return openServer(DEFAULT_PORT);
    }

    public static NetworkServer openServer(int port){
        try{
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            NetworkServer server = new NetworkServer(serverSocketChannel, selector);
            server.start();
            return server;
        } catch (IOException e) {
            System.out.printf("Failed to launch server at port %d%n", port);
            e.printStackTrace();
            return null;
        }
    }

    public void start(){
        this.thread = new Thread(this::networkLoop);
        this.thread.start();
    }

    public void networkLoop(){
        while (selector.isOpen()){
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if(!key.isValid()) continue;
                try{
                    if(key.isAcceptable()){
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ, new Session(clientChannel, selector));
                    }
                    else if(key.isValid() && key.isReadable()){
                        Session session = (Session) key.attachment();
                        session.handleRead(key);
                    }
                    else if(key.isValid() && key.isWritable()){
                        Session session = (Session) key.attachment();
                        session.handleWrite(key);
                    }
                }catch (IOException ex){
                    key.cancel();
                    System.out.println("Exception caught when handling network event");
                    ex.printStackTrace();
                    try {
                        key.channel().close();
                    } catch (IOException ex2) {
                        System.out.println("Exception caught when try closing connection because of latest exception");
                        ex2.printStackTrace();
                    }
                }
            }
        }
    }
}
