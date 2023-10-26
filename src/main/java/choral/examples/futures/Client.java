package choral.examples.futures;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import choral.runtime.IntToken;

public class Client {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        AsyncChannel_A<Object> ch = new AsyncChannel_A<Object>(
            Executors.newSingleThreadExecutor(),
            AsyncSocketByteChannel.connect( 
                JSONSerializer.getInstance(),
                Server.HOST, Server.PORT
            )
        );

        System.out.println("Connection succeeded.");

        HelloRoles_Client prot = new HelloRoles_Client();
        prot.sayHello(ch, "Hiya!", new IntToken(0));

        System.out.println("Done.");
    }
}
