package choral.examples.ozone.modelserving;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.ozone.modelserving.ModelServing_Client;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);
        SymChannel_A<Object> chW1 = AsyncSocketChannel.connect(
            new JavaSerializer(), Config.HOST, Config.WORKER1_PORT
        );
        SymChannel_A<Object> chW2 = AsyncSocketChannel.connect(
            new JavaSerializer(), Config.HOST, Config.WORKER2_PORT
        );
        AsyncChannel_A<Object> chB = new AsyncChannelImpl<>(
            threadPool,
            AsyncSocketChannel.connect( 
                new JavaSerializer(), Config.HOST, Config.BATCHER_PORT
            )
        );
        Log.debug("Connection succeeded.");

        ModelServing_Client prot = new ModelServing_Client(chW1, chW2, chB);
        //for (int i = 0; i < Config.NUM_ITERATIONS; i++)
        //    prot.on

        threadPool.shutdownNow();
        Log.debug("Client done.");
    }
}
