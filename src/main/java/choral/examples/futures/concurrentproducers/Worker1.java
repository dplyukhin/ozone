package choral.examples.futures.concurrentproducers;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.Log;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannel_A<String>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER1_PORT
            )
        );

        Log.debug("Connection succeeded.");

        WorkerState state = new WorkerState("Worker1", 0, Server.NUM_ITERATIONS);
        ConcurrentProducers_Worker1 prot = new ConcurrentProducers_Worker1();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < Server.NUM_ITERATIONS; i++) {
            prot.go(ch, state, String.valueOf(i), new Token(i));
        }
        try {
            state.iterationsLeft.await();
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);
        }
        catch (InterruptedException exn) {
            Log.debug("Interrupted while waiting for iterations to complete.");
        }
    }
}
