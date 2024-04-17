package choral.examples.ozone.concurrentproducers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Worker2 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);

        AsyncChannel_A<String> ch = new AsyncChannelImpl<>(
            threadPool,
            AsyncSocketChannel.connect( 
                new JavaSerializer(),
                Config.HOST, Config.WORKER2_PORT
            )
        );

        Log.debug("Connection succeeded.");

        WorkerState state = new WorkerState("Worker2", 0, Config.NUM_ITERATIONS);
        ConcurrentProducers_Worker2 prot = new ConcurrentProducers_Worker2();
        ch.select();

        for (int i = 0; i < Config.NUM_ITERATIONS; i++) {
            prot.go(ch, state, String.valueOf(i), new Token(i));
        }

        try {
            state.iterationsLeft.await();
            ch.select(Signal.START);
            Thread.sleep(1000);
        }
        catch (InterruptedException exn) {
            Log.debug("Interrupted while waiting for iterations to complete.");
        }

        Iterable<Float> latencies = state.getLatencies();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/concurrentproducers/worker2-latencies.csv"))) {
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
