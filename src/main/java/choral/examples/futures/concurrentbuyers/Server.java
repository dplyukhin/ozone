package choral.examples.futures.concurrentbuyers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import choral.channels.SymChannel_B;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncServerSocketByteChannel;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import choral.runtime.Token;

public class Server {
    public static final String HOST = "localhost";
    public static final int CLIENT1_PORT = 8668;
    public static final int CLIENT2_PORT = 8669;

    public static void main(String[] args) throws java.io.IOException {
        System.out.println("Running server...");

		AsyncServerSocketByteChannel client1_listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.CLIENT1_PORT 
            );
		AsyncServerSocketByteChannel client2_listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.CLIENT2_PORT 
            );

        AsyncChannel_B<String> ch_w1 = new AsyncChannel_B<String>( 
            Executors.newSingleThreadScheduledExecutor(),
            client1_listener.getNext()
        );
        System.out.println("Client1 connected.");

        AsyncChannel_B<String> ch_w2 = new AsyncChannel_B<String>( 
            Executors.newSingleThreadScheduledExecutor(),
            client2_listener.getNext()
        );
        System.out.println("Client2 connected.");

        ServerState state = new ServerState(1000);
        ConcurrentBuyers_Server prot = new ConcurrentBuyers_Server();
        prot.go(ch_w1, ch_w2, state, new Token(0));

        client1_listener.close();
        client2_listener.close();
        System.out.println("Done.");
    }
}
