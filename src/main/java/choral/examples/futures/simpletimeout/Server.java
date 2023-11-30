package choral.examples.futures.simpletimeout;

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
import choral.runtime.UnreliableAsyncChannel_B;
import choral.examples.futures.hello.HelloRoles_Server;

public class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 8667;

    public static void main(String[] args) throws java.io.IOException {
        System.out.println("Running server...");
        // Run server and create a channel from the first open connection
		AsyncServerSocketByteChannel listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.PORT 
            );

        UnreliableAsyncChannel_B<String> ch = new UnreliableAsyncChannel_B<String>( 
            Executors.newScheduledThreadPool(2),
            listener.getNext()
        );
        System.out.println("Client connected.");

        SimpleTimeout_Server prot = new SimpleTimeout_Server();
        prot.go(ch, new Token(0));

        listener.close();
        System.out.println("Done.");
    }
}
