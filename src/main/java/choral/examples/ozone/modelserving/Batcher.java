package choral.examples.ozone.modelserving;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.AsyncChannel_B;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.ozone.modelserving.ModelServing_Client;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Batcher {

    public static void debug(String msg) {
        Log.debug("BATCHER: " + msg);
    }

    public static void main(String[] args) {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(5);

        debug("Connecting to other nodes...");

        AsyncChannel_B<Object> chC = new AsyncChannelImpl<>(
            threadPool,
            AsyncSocketChannel.connect(new JavaSerializer(), Config.HOST, Config.CLIENT_FOR_BATCHER)
        );
        debug("Connected to client.");

        AsyncServerSocketChannel model1_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.BATCHER_FOR_MODEL1
        );
        AsyncServerSocketChannel model2_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.BATCHER_FOR_MODEL2
        );
        AsyncServerSocketChannel worker1_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.BATCHER_FOR_WORKER1
        );
        AsyncServerSocketChannel worker2_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.BATCHER_FOR_WORKER2
        );

        try {
            AsyncChannel_A<Object> chM1 = new AsyncChannelImpl<Object>( 
                threadPool, model1_listener.getNext()
            );
            AsyncChannel_A<Object> chM2 = new AsyncChannelImpl<Object>( 
                threadPool, model2_listener.getNext()
            );
            debug("Models connected.");
            SymChannel_A<Object> chW1 = worker1_listener.getNext();
            SymChannel_A<Object> chW2 = worker2_listener.getNext();
            debug("Workers connected.");

            chW1.select();
            chW2.select();
            chC.<BenchmarkReady>select(BenchmarkReady.READY);

            debug("Starting!");

            ModelServing_Batcher prot = new ModelServing_Batcher(chC, chM1, chM2, chW1, chW2);
            BatcherState state = new BatcherState();
            prot.onImage(state, new Token(0));
            //for (int i = 0; i < Config.NUM_ITERATIONS; i++)
            //    prot.on

        } 
        catch (IOException e) {
            debug("Aborting: " + e.getMessage());
        }
        finally {
            threadPool.shutdownNow();
            model1_listener.close();
            model2_listener.close();
            worker1_listener.close();
            worker2_listener.close();
            debug("Done.");
        }
    }
}
