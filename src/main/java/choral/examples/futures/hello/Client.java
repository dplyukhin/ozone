package choral.examples.futures.hello;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;

import choral.examples.futures.hello.HelloRoles_Client;

public class Client {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        AsyncChannel_A<Object> ch = new AsyncChannel_A<Object>(
            Executors.newSingleThreadExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.PORT
            )
        );

        System.out.println("Connection succeeded.");

        HelloRoles_Client prot = new HelloRoles_Client();
        prot.sayHello(ch, "Hiya!", new Token(0));

        System.out.println("Done.");
    }
}
