package choral.examples.futures.playground;

import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;

public class Client {

    public static void main(String[] args) {
        AsyncSocketChannel ch =
            AsyncSocketChannel.connect(
                new JavaSerializer(),
                Server.HOST, Server.PORT
            );

        for (int i = 0; i < 50; i++) {
            System.out.println("Iteration " + i + ": Client starting at time " + System.currentTimeMillis());
            ch.< String >com( "Hi" );
            ch.< String >com();
            System.out.println("Iteration " + i + ": Client ending at time " + System.currentTimeMillis());
            //try { Thread.sleep(100); } catch (InterruptedException e) { }
        }
    }
}
