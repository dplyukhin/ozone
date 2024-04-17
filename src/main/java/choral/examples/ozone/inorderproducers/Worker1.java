package choral.examples.ozone.inorderproducers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import choral.Log;
import choral.channels.SymChannel_A;
import choral.examples.ozone.concurrentproducers.InOrderProducers_Worker1;
import choral.examples.ozone.concurrentproducers.Signal;
import choral.examples.ozone.concurrentproducers.WorkerState;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        SymChannel_A<Object> ch = 
            AsyncSocketChannel.connect(
                new JavaSerializer(),
                Config.HOST, Config.WORKER1_PORT
            );

        Log.debug("Connection succeeded.");

        WorkerState state = new WorkerState("Worker1", 0, Config.NUM_ITERATIONS);
        InOrderProducers_Worker1 prot = new InOrderProducers_Worker1();
        ch.select();

        long benchmarkStartTime = System.currentTimeMillis();
        long iterationStartTime = benchmarkStartTime;
        long[] latencies = new long[Config.NUM_ITERATIONS];

        for (int i = 0; i < Config.NUM_ITERATIONS; i++) {
            prot.go(ch, state, String.valueOf(i));
            latencies[i] = System.currentTimeMillis() - iterationStartTime;

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

        String filename = "data/inorderproducers/worker1-rps" + Config.REQUESTS_PER_SECOND + ".csv";

        try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        ) {
            for (long value : latencies) {
                writer.write(Float.toString(value));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
