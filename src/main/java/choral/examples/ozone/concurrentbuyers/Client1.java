package choral.examples.ozone.concurrentbuyers;

import java.util.concurrent.Executors;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Client1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannelImpl<String>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketChannel.connect( 
                new JavaSerializer(),
                Config.HOST, Config.CLIENT1_PORT
            )
        );

        Log.debug("Connection succeeded.");

        ConcurrentBuyers_Client1 prot = new ConcurrentBuyers_Client1();
        ClientState state = new ClientState();
        prot.go(ch, state, new Token(0));
    }
}
