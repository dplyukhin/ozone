package choral.examples.futures;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import choral.runtime.MyWrapperImpl_A;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;

public class Client {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        MyWrapperImpl_A<Object> ch = new MyWrapperImpl_A<Object>(
            new SerializerChannel_A( 
                JSONSerializer.getInstance(),
                new WrapperByteChannel_A( 
                    SocketByteChannel.connect( Server.HOST, Server.PORT ) 
                )
            )
        );

        System.out.println("Connection succeeded.");

        HelloRoles_Client prot = new HelloRoles_Client();
        prot.sayHello(ch, "iya!");

        System.out.println("Done.");
    }
}
