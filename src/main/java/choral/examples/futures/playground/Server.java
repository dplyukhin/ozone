package choral.examples.futures.playground;

import java.io.IOException;
import java.nio.ByteBuffer;

import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.WrapperByteChannel.WrapperByteChannelImpl;

public class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 8667;

    public static void main(String[] args) throws java.io.IOException {
        MyServerSocketByteChannel client_listener =
            MyServerSocketByteChannel.at( 
                Server.HOST, Server.PORT 
            );

        WrapperByteChannelImpl ch = 
            new WrapperByteChannelImpl(
                client_listener.getNext()
            );

        for (int i = 0; i < 50; i++) {
            ch.com();
            System.out.println("Iteration " + i + ": Server starting at time " + System.currentTimeMillis());
            ch.com( ByteBuffer.wrap(new byte[] { 1, 1, 1, 1 }) );
            // try { Thread.sleep(5); } catch (InterruptedException e) { }
            //ch.< Integer >com(1);
            System.out.println("Iteration " + i + ": Server completed at time " + System.currentTimeMillis());
        }
    }
}
