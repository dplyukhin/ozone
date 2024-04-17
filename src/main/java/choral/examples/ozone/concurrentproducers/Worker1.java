package choral.examples.ozone.concurrentproducers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);

        AsyncChannel_A<String> ch = new AsyncChannelImpl<>(
            threadPool,
            AsyncSocketChannel.connect( 
                new JavaSerializer(),
                Config.HOST, Config.WORKER1_PORT
            )
        );

        Log.debug("Connection succeeded.");

        WorkerState state = new WorkerState("Worker1", 0, Config.NUM_ITERATIONS);
        ConcurrentProducers_Worker1 prot = new ConcurrentProducers_Worker1();
        ch.select();

        long benchmarkStartTime = System.currentTimeMillis();
        long iterationStartTime = benchmarkStartTime;

        for (int i = 0; i < Config.NUM_ITERATIONS; i++) {
            prot.go(ch, state, String.valueOf(i), new Token(i));
            
            // Sleep until the request interval has elapsed
            iterationStartTime += Config.REQUEST_INTERVAL;
            long sleepTime = iterationStartTime - System.currentTimeMillis();
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Log.debug("Interrupted while waiting for request interval to elapse.");
                }
            }
        }

        try {
            state.iterationsLeft.await();
            ch.select(Signal.START);
            Thread.sleep(1000);
        }
        catch (InterruptedException exn) {
            Log.debug("Interrupted while waiting for iterations to complete.");
        }
        long endTime = System.currentTimeMillis();

        Iterable<Float> latencies = state.getLatencies();
        String filename = "data/concurrentproducers/worker1-rps" + Config.REQUESTS_PER_SECOND + ".csv";

        try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        ) {
            for (float value : latencies) {
                writer.write(Float.toString(value));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadPool.shutdownNow();
    }
}
