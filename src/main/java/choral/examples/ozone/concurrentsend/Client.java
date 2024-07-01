package choral.examples.ozone.concurrentsend;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_B;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
        AsyncChannel_B<Object> ch = new AsyncChannelImpl<>(
            threadPool,
            AsyncSocketChannel.connect( 
                new JavaSerializer(),
                Config.SERVER_HOST, Config.CLIENT_PORT
            )
        );

        Log.debug("Connection succeeded.");

        ConcurrentSend_Client prot = new ConcurrentSend_Client();
        ClientState state = new ClientState(Config.NUM_ITERATIONS);
        for (int i = 0; i < Config.NUM_ITERATIONS; i++)
            prot.concurrentFetchAndForward(ch, state, new Token(i));

        state.await();
        try {
            Thread.sleep(20000); // Give client time to send final message
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdownNow();
        Log.debug("Client done.");
    }
}
