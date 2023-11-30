package choral.examples.futures.simpletimeout;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.runtime.UnreliableAsyncChannel_A;
import choral.examples.futures.hello.HelloRoles_Client;

public class Client {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        UnreliableAsyncChannel_A<String> ch = new UnreliableAsyncChannel_A<String>(
            Executors.newScheduledThreadPool(2),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.PORT
            )
        );

        System.out.println("Connection succeeded.");

        SimpleTimeout_Client prot = new SimpleTimeout_Client();
        prot.go(ch, new Token(0));

        System.out.println("Done.");
    }
}
