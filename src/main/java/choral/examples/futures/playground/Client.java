package choral.examples.futures.playground;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

    public static void main(String[] args) {
        try{
            SocketChannel ch = SocketChannel.open();
            ch.connect( new InetSocketAddress( Server.HOST, Server.PORT ) );
            ch.configureBlocking( true );

            for (int i = 0; i < 50; i++) {
                System.out.println("Iteration " + i + ": Client starting at time " + System.currentTimeMillis());
                ch.write( ByteBuffer.wrap(new byte[] { 1, 1, 1, 1 }));
                ch.read(ByteBuffer.allocate(4));
                System.out.println("Iteration " + i + ": Client ending at time " + System.currentTimeMillis());
                //try { Thread.sleep(100); } catch (InterruptedException e) { }
            }
        }
        catch (IOException e) {}
    }
}
