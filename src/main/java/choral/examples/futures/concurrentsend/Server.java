package choral.examples.futures.concurrentsend;

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
import choral.Log;

import choral.examples.futures.concurrentsend.*;

public class Server {
    public static final String HOST = "localhost";
    public static final int CLIENT_PORT = 8667;
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

		AsyncServerSocketByteChannel client_listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.CLIENT_PORT 
            );
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

        AsyncChannel_A<String> ch_c = new AsyncChannel_A<String>( 
            Executors.newSingleThreadScheduledExecutor(),
            client_listener.getNext()
        );
        Log.debug("Client connected.");

        AsyncChannel_B<String> ch_w1 = new AsyncChannel_B<String>( 
            Executors.newSingleThreadScheduledExecutor(),
            worker1_listener.getNext()
        );
        Log.debug("Worker1 connected.");

        AsyncChannel_B<String> ch_w2 = new AsyncChannel_B<String>( 
            Executors.newSingleThreadScheduledExecutor(),
            worker2_listener.getNext()
        );
        Log.debug("Worker2 connected.");

        ConcurrentSend_Server prot = new ConcurrentSend_Server();
        prot.go(ch_w1, ch_w2, ch_c, new Token(0));

        client_listener.close();
        Log.debug("Done.");
    }
}
