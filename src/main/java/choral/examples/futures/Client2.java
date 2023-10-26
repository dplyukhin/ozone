package choral.examples.futures;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import choral.lang.Unit;
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

        AsyncChannel_A<Object> ch = new AsyncChannel_A<Object>(
            Executors.newSingleThreadExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.PORT
            )
        );

        ch.com("Hello from client", 1, new IntToken(0), Unit.id, Unit.id);
        System.out.println("Sent hello");
    }
}
