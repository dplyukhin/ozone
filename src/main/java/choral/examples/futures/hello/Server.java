package choral.examples.futures.hello;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import choral.channels.SymChannel_B;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncServerSocketByteChannel;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import choral.runtime.Token;

import choral.examples.futures.hello.HelloRoles_Server;
import choral.Log;

public class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 8667;

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");
        // Run server and create a channel from the first open connection
		AsyncServerSocketByteChannel listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.PORT 
            );

        AsyncChannel_B<Object> ch = new AsyncChannel_B<Object>( 
            Executors.newSingleThreadScheduledExecutor(),
            listener.getNext()
        );
        Log.debug("Client connected.");

        HelloRoles_Server prot = new HelloRoles_Server();
        prot.sayHello(ch, new Token(0));

        listener.close();
        Log.debug("Done.");
    }
}
