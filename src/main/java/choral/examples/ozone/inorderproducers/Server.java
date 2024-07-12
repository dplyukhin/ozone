package choral.examples.ozone.inorderproducers;

import choral.Log;
import choral.channels.SymChannel_B;
import choral.examples.ozone.concurrentproducers.InOrderProducers_Server;
import choral.examples.ozone.concurrentproducers.ServerState;
import choral.examples.ozone.concurrentproducers.Signal;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.JavaSerializer;

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

        SymChannel_B< Object > ch_w1 = worker1_listener.getNext();
        Log.debug("Worker1 connected.");

        SymChannel_B< Object > ch_w2 = worker2_listener.getNext();
        Log.debug("Worker2 connected.");

        ServerState state = new ServerState();
        InOrderProducers_Server prot = new InOrderProducers_Server();
        ch_w1.select(Signal.START);
        ch_w2.select(Signal.START);

        for (int i = 0; i < Config.NUM_ITERATIONS; i++) {
            prot.go(ch_w1, ch_w2, state);
        }

        ch_w1.select();
        ch_w2.select();

        worker1_listener.close();
        worker2_listener.close();
        Log.debug("Done.");
    }
}
