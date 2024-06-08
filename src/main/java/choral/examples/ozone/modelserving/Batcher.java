package choral.examples.ozone.modelserving;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.AsyncChannel_B;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.ozone.modelserving.ConcurrentServing_Client;
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

        Object chC;
        if (Config.USE_OZONE) {
            chC = new AsyncChannelImpl<>(threadPool, 
                AsyncSocketChannel.connect(
                    new JavaSerializer(), Config.CLIENT_HOST, Config.CLIENT_FOR_BATCHER
                )
            );
        }
        else {
            chC = AsyncSocketChannel.connect(
                new JavaSerializer(), Config.CLIENT_HOST, Config.CLIENT_FOR_BATCHER
            );
        }
        debug("Connected to client.");

        AsyncServerSocketChannel model1_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.BATCHER_HOST, Config.BATCHER_FOR_MODEL1
        );
        AsyncServerSocketChannel model2_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.BATCHER_HOST, Config.BATCHER_FOR_MODEL2
        );
        AsyncServerSocketChannel worker1_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.BATCHER_HOST, Config.BATCHER_FOR_WORKER1
        );
        AsyncServerSocketChannel worker2_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.BATCHER_HOST, Config.BATCHER_FOR_WORKER2
        );

        try {
            Object chM1;
            Object chM2;
            if (Config.USE_OZONE) {
                chM1 = new AsyncChannelImpl<Object>(threadPool, model1_listener.getNext());
                chM2 = new AsyncChannelImpl<Object>(threadPool, model2_listener.getNext());
            }
            else {
                chM1 = model1_listener.getNext();
                chM2 = model2_listener.getNext();
            }
            debug("Models connected.");

            SymChannel_A<Object> chW1 = worker1_listener.getNext();
            SymChannel_A<Object> chW2 = worker2_listener.getNext();
            debug("Workers connected.");

            if (Config.USE_OZONE) {
                ((AsyncChannel_B<Object>) chC).<BenchmarkReady>select(BenchmarkReady.READY);
            }
            else {
                ((SymChannel_B<Object>) chC).<BenchmarkReady>select(BenchmarkReady.READY);
            }

            debug("Starting!");

            BatcherState state = new BatcherState();

            for (int i = 0; i < Config.WARMUP_ITERATIONS + Config.NUM_REQUESTS; i++) {

                if (Config.USE_OZONE) {
                    AsyncChannel_B<Object> chC_async = (AsyncChannel_B<Object>) chC;
                    AsyncChannel_A<Object> chM1_async = (AsyncChannel_A<Object>) chM1;
                    AsyncChannel_A<Object> chM2_async = (AsyncChannel_A<Object>) chM2;

                    new ConcurrentServing_Batcher(chC_async, chM1_async, chM2_async, chW1, chW2)
                        .onImage(state, new Token(i));
                }
                else {
                    SymChannel_B<Object> chC_sync = (SymChannel_B<Object>) chC;
                    SymChannel_A<Object> chM1_sync = (SymChannel_A<Object>) chM1;
                    SymChannel_A<Object> chM2_sync = (SymChannel_A<Object>) chM2;

                    new InOrderServing_Batcher(chC_sync, chM1_sync, chM2_sync, chW1, chW2)
                        .onImage(state);
                }

            }

            if (Config.USE_OZONE) {
                debug("Sleeping while waiting for futures to complete...");
                try {
                    Thread.sleep(20000);
                }
                catch (InterruptedException e) {
                    debug("Sleep interrupted: " + e.getMessage());
                }
            }

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
