package choral.examples.futures.concurrentbuyers;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;

public class Client2 {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannel_A<String>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.CLIENT2_PORT
            )
        );

        System.out.println("Connection succeeded.");

        ConcurrentBuyers_Client2 prot = new ConcurrentBuyers_Client2();
        ClientState state = new ClientState();
        prot.go(ch, state, new Token(0));
    }
}