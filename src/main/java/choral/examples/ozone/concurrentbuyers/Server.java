package choral.examples.ozone.concurrentbuyers;

import java.util.concurrent.Executors;

import choral.Log;
import choral.channels.AsyncChannel_B;
import choral.examples.ozone.concurrentbuyers.ConcurrentBuyers_Server;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Server {
    public static final String HOST = "localhost";
    public static final int CLIENT1_PORT = 8668;
    public static final int CLIENT2_PORT = 8669;

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

		AsyncServerSocketChannel client1_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(), 
                Server.HOST, Server.CLIENT1_PORT 
            );
		AsyncServerSocketChannel client2_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(), 
                Server.HOST, Server.CLIENT2_PORT 
            );

        AsyncChannel_B<String> ch_w1 = new AsyncChannelImpl<String>( 
            Executors.newSingleThreadScheduledExecutor(),
            client1_listener.getNext()
        );
        Log.debug("Client1 connected.");

        AsyncChannel_B<String> ch_w2 = new AsyncChannelImpl<String>( 
            Executors.newSingleThreadScheduledExecutor(),
            client2_listener.getNext()
        );
        Log.debug("Client2 connected.");

        ServerState state = new ServerState(0);
        ConcurrentBuyers_Server prot = new ConcurrentBuyers_Server();
        prot.go(ch_w1, ch_w2, state, new Token(0));

        client1_listener.close();
        client2_listener.close();
        Log.debug("Done.");
    }
}
