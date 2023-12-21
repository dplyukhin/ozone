package choral.examples.futures.concurrentproducers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import choral.Log;
import choral.examples.futures.Scheduler;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Token;
import choral.runtime.Serializers.KryoSerializer;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannelImpl<String>(
            Executors.newScheduledThreadPool(4),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER1_PORT
            )
        );

        Log.debug("Connection succeeded.");

        WorkerState state = new WorkerState("Worker1", 0, Server.NUM_ITERATIONS);
        ConcurrentProducers_Worker1 prot = new ConcurrentProducers_Worker1();
        long startTime = System.currentTimeMillis();
        new Scheduler().schedule(
            i -> prot.go(ch, state, String.valueOf(i), new Token(i)), 
            Server.ITERATION_PERIOD_MILLIS,
            Server.NUM_ITERATIONS
        );
        try {
            state.iterationsLeft.await();
            Thread.sleep(1000);
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);

            Iterable<Float> latencies = state.getLatencies();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/concurrentproducers/worker1-latencies.csv"))) {
                for (float value : latencies) {
                    writer.write(Float.toString(value));
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
