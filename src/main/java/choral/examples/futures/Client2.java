package choral.examples.futures;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import choral.runtime.IntToken;

public class Client2 {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        SerializerChannel_A ch = new SerializerChannel_A( 
            JSONSerializer.getInstance(),
            AsyncSocketByteChannel.connect( Server.HOST, Server.PORT ) 
        );

        executor.submit(() -> {
            System.out.println("Listening...");
            while (true) {
                String foo = ch.com();
                System.out.println("Got: " + foo);
            }
        });

        ch.com("Hello from client");
        System.out.println("Sent hello");
    }
}
