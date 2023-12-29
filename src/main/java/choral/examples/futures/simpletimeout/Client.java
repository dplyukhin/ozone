package choral.examples.futures.simpletimeout;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.runtime.UnreliableAsyncChannel;
import choral.examples.futures.hello.HelloRoles_Client;
import choral.Log;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new UnreliableAsyncChannel<String>(
            Executors.newScheduledThreadPool(2),
            AsyncSocketChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.PORT
            )
        );

        Log.debug("Connection succeeded.");

        SimpleTimeout_Client prot = new SimpleTimeout_Client();
        prot.go(ch, new Token(0));

        Log.debug("Done.");
    }
}
