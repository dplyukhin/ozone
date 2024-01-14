package choral.examples.ozone.concurrentsend;

import java.util.concurrent.Executors;

import choral.Log;
import choral.channels.AsyncChannel_B;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_B<Object> ch = new AsyncChannelImpl<Object>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketChannel.connect( 
                new JavaSerializer(),
                Config.HOST, Config.CLIENT_PORT
            )
        );

        Log.debug("Connection succeeded.");

        ConcurrentSend_Client prot = new ConcurrentSend_Client();
        for (int i = 0; i < Config.NUM_ITERATIONS; i++)
            prot.concurrentFetchAndForward(ch, new Token(i));
    }
}
