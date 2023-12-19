package choral.examples.futures.concurrentproducers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;


import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.DelayableAsyncChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.Log;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new DelayableAsyncChannel<String>(
            Executors.newScheduledThreadPool(4),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER1_PORT
            ),
            Server.MAX_DELAY_MILLIS
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

            Iterable<Long> latencies = state.getLatencies();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("worker1-latencies.csv"))) {
                for (long value : latencies) {
                    writer.write(Long.toString(value));
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (InterruptedException exn) {
            Log.debug("Interrupted while waiting for iterations to complete.");
        }
    }
}
