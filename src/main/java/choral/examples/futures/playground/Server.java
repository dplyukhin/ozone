package choral.examples.futures.playground;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import choral.channels.SymChannel_B;
import choral.lang.Unit;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncServerSocketByteChannel;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.DataMsg;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import choral.runtime.Token;
import choral.runtime.IntegrityKey;

public class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 8667;

    public static void main(String[] args) throws java.io.IOException {
        System.out.println("Running server...");
        KryoSerializer serializer = KryoSerializer.getInstance();

        // System.out.println("" + serializer.toObject(serializer.fromObject(new AsyncMessage.DataMsg(new IntegrityKey(0, new Token(0)), "Hello from server"))));

        // Run server and create a channel from the first open connection
		AsyncServerSocketByteChannel listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.PORT 
            );

        AsyncSocketByteChannel ch = //new AsyncChannel_B<Object>( 
            //Executors.newSingleThreadExecutor(),
            listener.getNext();
        //);
        System.out.println("Client connected.");

        ch.com(new IntegrityKey(0, new Token(0)));
        System.out.println("Sent hello");
        System.out.println("Got:" + ch.com());
    }
}