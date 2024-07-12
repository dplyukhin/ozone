package choral.examples.ozone.concurrentproducers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_B;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Server {

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

		AsyncServerSocketChannel worker1_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(), 
                Config.SERVER_HOST, Config.WORKER1_PORT
            );
		AsyncServerSocketChannel worker2_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(), 
                Config.SERVER_HOST, Config.WORKER2_PORT
            );

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);

        AsyncChannel_B<String> ch_w1 = new AsyncChannelImpl<>(
            threadPool,
            worker1_listener.getNext()
        );
        Log.debug("Worker1 connected.");

        AsyncChannel_B<String> ch_w2 = new AsyncChannelImpl<>(
            threadPool,
            worker2_listener.getNext()
        );
        Log.debug("Worker2 connected.");

        ServerState state = new ServerState();
        ConcurrentProducers_Server prot = new ConcurrentProducers_Server();
        ch_w1.select(Signal.START);
        ch_w2.select(Signal.START);

        for (int i = 0; i < Config.NUM_ITERATIONS; i++) {
            prot.go(ch_w1, ch_w2, state, new Token(i));
        }

        ch_w1.select();
        ch_w2.select();

        worker1_listener.close();
        worker2_listener.close();
        threadPool.shutdownNow();
        Log.debug("Done.");
    }
}
