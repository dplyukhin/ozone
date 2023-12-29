package choral.examples.futures.concurrentbuyers;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.Log;

public class Client2 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannelImpl<String>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.CLIENT2_PORT
            )
        );

        Log.debug("Connection succeeded.");

        ConcurrentBuyers_Client2 prot = new ConcurrentBuyers_Client2();
        ClientState state = new ClientState();
        prot.go(ch, state, new Token(0));
    }
}
