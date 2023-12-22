package choral.examples.futures.inorderproducers;

import choral.Log;
import choral.channels.SymChannel_B;
import choral.examples.futures.concurrentproducers.InOrderProducers_Server;
import choral.examples.futures.concurrentproducers.ServerState;
import choral.examples.futures.concurrentproducers.Signal;
import choral.runtime.JavaSerializer;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.WrapperByteChannel.WrapperByteChannelImpl;

public class Server {
    public static final String HOST = "localhost";
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int NUM_ITERATIONS = 500;
    public static final int ITERATION_PERIOD_MILLIS = 100;

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

		ServerSocketByteChannel worker1_listener =
            ServerSocketByteChannel.at( 
                Server.HOST, Server.WORKER1_PORT 
            );
		ServerSocketByteChannel worker2_listener =
            ServerSocketByteChannel.at( 
                Server.HOST, Server.WORKER2_PORT 
            );

        SymChannel_B<Object> ch_w1 = 
            new SerializerChannel_B(
                new JavaSerializer(),
                new WrapperByteChannelImpl(
                    worker1_listener.getNext()
                )
            );
        Log.debug("Worker1 connected.");

        SymChannel_B<Object> ch_w2 = 
            new SerializerChannel_B(
                new JavaSerializer(),
                new WrapperByteChannelImpl(
                    worker2_listener.getNext()
                )
            );
        Log.debug("Worker2 connected.");

        ServerState state = new ServerState();
        InOrderProducers_Server prot = new InOrderProducers_Server();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            ch_w1.select(Signal.START);
            ch_w2.select(Signal.START);
            prot.go(ch_w1, ch_w2, state);
            try {
                Thread.sleep(ITERATION_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        worker1_listener.close();
        worker2_listener.close();
        Log.debug("Done.");
    }
}
