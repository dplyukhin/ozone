package choral.examples.futures;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import choral.channels.SymChannel_B;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncServerSocketByteChannel;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import choral.runtime.IntToken;

public class Server2 {
    public static final String HOST = "localhost";
    public static final int PORT = 8667;

    public static void main(String[] args) throws java.io.IOException {
        System.out.println("Running server...");
        // Run server and create a channel from the first open connection
		AsyncServerSocketByteChannel listener =
				AsyncServerSocketByteChannel.at( Server.HOST, Server.PORT );

        ExecutorService executor = Executors.newSingleThreadExecutor();

        SerializerChannel_B ch = 
            new SerializerChannel_B(
                JSONSerializer.getInstance(),
                listener.getNext()
            );
        System.out.println("Client connected.");


        executor.submit(() -> {
            System.out.println("Listening...");
            while (true) {
                String foo = ch.com();
                System.out.println("Got: " + foo);
            }
        });

        ch.com("Hello from server");
        System.out.println("Sent hello");
    }
}
