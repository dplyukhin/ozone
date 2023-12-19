package choral.examples.futures.inorderproducers;

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
import choral.runtime.DelayableAsyncChannel;
import choral.runtime.DelayableChannel;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import choral.runtime.Token;
import choral.Log;

import choral.examples.futures.concurrentproducers.WorkerState;
import choral.examples.futures.concurrentproducers.ServerState;
import choral.examples.futures.concurrentproducers.InOrderProducers_Worker1;
import choral.examples.futures.concurrentproducers.InOrderProducers_Worker2;
import choral.examples.futures.concurrentproducers.InOrderProducers_Server;

public class Server {
    public static final String HOST = "localhost";
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int NUM_ITERATIONS = 500;
    public static final int MAX_DELAY_MILLIS = 5;

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

		ServerSocketByteChannel worker1_listener =
            ServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.WORKER1_PORT 
            );
		ServerSocketByteChannel worker2_listener =
            ServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.WORKER2_PORT 
            );

        SymChannel_B<String> ch_w1 = new DelayableChannel<String>( 
            worker1_listener.getNext(),
            MAX_DELAY_MILLIS
        );
        Log.debug("Worker1 connected.");

        SymChannel_B<String> ch_w2 = new DelayableChannel<String>( 
            worker2_listener.getNext(),
            MAX_DELAY_MILLIS
        );
        Log.debug("Worker2 connected.");

        ServerState state = new ServerState(5);
        InOrderProducers_Server prot = new InOrderProducers_Server();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            prot.go(ch_w1, ch_w2, state, new Token(i));
        }

        worker1_listener.close();
        worker2_listener.close();
        Log.debug("Done.");
    }
}
