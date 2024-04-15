package choral.examples.ozone.modelserving;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.ozone.modelserving.ModelServing_Client;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;

public class Client {

    public static void main(String[] args) {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);

        Log.debug("Connecting to other nodes...");

        AsyncServerSocketChannel worker1_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.WORKER1_PORT
        );
        AsyncServerSocketChannel worker2_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.WORKER2_PORT
        );
        AsyncServerSocketChannel batcher_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.BATCHER_PORT
        );

        try {
            SymChannel_A<Object> chW1 = worker1_listener.getNext();
            Log.debug("Worker 1 connected.");
            SymChannel_A<Object> chW2 = worker2_listener.getNext();
            Log.debug("Worker 2 connected.");
            AsyncChannel_A<Object> chB = new AsyncChannelImpl<Object>( 
                threadPool, batcher_listener.getNext()
            );
            Log.debug("Batcher connected.");

            Log.debug("Client starting!");

            ModelServing_Client prot = new ModelServing_Client(chW1, chW2, chB);
            //for (int i = 0; i < Config.NUM_ITERATIONS; i++)
            //    prot.on

        } 
        catch (IOException e) {
            Log.debug("Error in client, aborting: " + e.getMessage());
        }
        finally {
            threadPool.shutdownNow();
            worker1_listener.close();
            worker2_listener.close();
            batcher_listener.close();
            Log.debug("Client done.");
        }
    }
}
