package choral.examples.futures.hello;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.Log;

import choral.examples.futures.hello.HelloRoles_Client;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<Object> ch = new AsyncChannelImpl<Object>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.PORT
            )
        );

        Log.debug("Connection succeeded.");

        HelloRoles_Client prot = new HelloRoles_Client();
        prot.sayHello(ch, "Hiya!", new Token(0));

        Log.debug("Done.");
    }
}
