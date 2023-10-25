package choral.examples.futures;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import choral.channels.SymChannel_B;
import choral.runtime.AsyncChannel_B;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;

public class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 8667;

    public static void main(String[] args) throws java.io.IOException {
        System.out.println("Running server...");
        // Run server and create a channel from the first open connection
		ServerSocketByteChannel listener =
				ServerSocketByteChannel.at( Server.HOST, Server.PORT );

        AsyncChannel_B<Object> ch = new AsyncChannel_B<Object>( 
            new SerializerChannel_B(
                JSONSerializer.getInstance(),
                new WrapperByteChannel_B( listener.getNext() )
            )
        );
        System.out.println("Client connected.");

        HelloRoles_Server prot = new HelloRoles_Server();
        prot.sayHello(ch);

        listener.close();
        System.out.println("Done.");
    }
}
