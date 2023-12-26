package choral.examples.futures.playground;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 8667;

    public static void main(String[] args) throws java.io.IOException {
        try {
            ServerSocketChannel client_listener = ServerSocketChannel.open();
            client_listener
                    .socket()
                    .bind( new InetSocketAddress( Server.HOST, Server.PORT ) );
            
            SocketChannel ch = client_listener.accept();
            ch.configureBlocking( true );

            for (int i = 0; i < 50; i++) {
                ch.read( ByteBuffer.allocate(4) );
                System.out.println("Iteration " + i + ": Server starting at time " + System.currentTimeMillis());
                ch.write( ByteBuffer.wrap(new byte[] { 1, 1, 1, 1 }) );
                // try { Thread.sleep(5); } catch (InterruptedException e) { }
                //ch.< Integer >com(1);
                System.out.println("Iteration " + i + ": Server completed at time " + System.currentTimeMillis());
            }
        }
        catch (IOException e) {}
    }
}
