package choral.examples.futures.concurrentproducers;

import java.util.concurrent.Executors;

import choral.Log;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncServerSocketByteChannel;
import choral.runtime.Token;
import choral.runtime.Serializers.KryoSerializer;

public class Server {
    public static final String HOST = "localhost";
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int NUM_ITERATIONS = 1000;
    public static final int ITERATION_PERIOD_MILLIS = 20;
    public static final long SERVER_MAX_COMPUTE_TIME_MILLIS = 5;

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

		AsyncServerSocketByteChannel worker1_listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.WORKER1_PORT 
            );
		AsyncServerSocketByteChannel worker2_listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.WORKER2_PORT 
            );

        AsyncChannel_B<String> ch_w1 = new AsyncChannelImpl<String>( 
            Executors.newScheduledThreadPool(4),
            worker1_listener.getNext()
        );
        Log.debug("Worker1 connected.");

        AsyncChannel_B<String> ch_w2 = new AsyncChannelImpl<String>( 
            Executors.newScheduledThreadPool(4),
            worker2_listener.getNext()
        );
        Log.debug("Worker2 connected.");

        ServerState state = new ServerState();
        ConcurrentProducers_Server prot = new ConcurrentProducers_Server();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            ch_w1.select(Signal.START);
            ch_w2.select(Signal.START);
            prot.go(ch_w1, ch_w2, state, new Token(i));
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
