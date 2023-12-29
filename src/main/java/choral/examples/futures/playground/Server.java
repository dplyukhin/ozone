package choral.examples.futures.playground;

import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;

public class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 8667;

    public static void main(String[] args) throws java.io.IOException {
        AsyncServerSocketChannel client_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(),
                Server.HOST, Server.PORT 
            );

        AsyncSocketChannel ch = client_listener.getNext();

        for (int i = 0; i < 50; i++) {
            ch.< String >com();
            System.out.println("Iteration " + i + ": Server starting at time " + System.currentTimeMillis());
            ch.< String >com( "Hey client" );
            // try { Thread.sleep(5); } catch (InterruptedException e) { }
            //ch.< Integer >com(1);
            System.out.println("Iteration " + i + ": Server completed at time " + System.currentTimeMillis());
        }
    }
}
