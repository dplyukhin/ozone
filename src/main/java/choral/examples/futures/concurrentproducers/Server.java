package choral.examples.futures.concurrentproducers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import choral.channels.SymChannel_B;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncServerSocketByteChannel;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import choral.runtime.Token;
import choral.Log;

public class Server {
    public static final String HOST = "localhost";
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int NUM_ITERATIONS = 500;

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
            Executors.newSingleThreadScheduledExecutor(),
            worker1_listener.getNext()
        );
        Log.debug("Worker1 connected.");

        AsyncChannel_B<String> ch_w2 = new AsyncChannelImpl<String>( 
            Executors.newSingleThreadScheduledExecutor(),
            worker2_listener.getNext()
        );
        Log.debug("Worker2 connected.");

        ServerState state = new ServerState(5);
        ConcurrentProducers_Server prot = new ConcurrentProducers_Server();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            prot.go(ch_w1, ch_w2, state, new Token(i));
        }

        worker1_listener.close();
        worker2_listener.close();
        Log.debug("Done.");
    }
}
