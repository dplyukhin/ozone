package choral.examples.ozone.inorderproducers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import choral.Log;
import choral.channels.SymChannel_A;
import choral.examples.ozone.concurrentproducers.InOrderProducers_Worker1;
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
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < Config.NUM_ITERATIONS; i++) {
            ch.select();
            prot.go(ch, state, String.valueOf(i));
        }
        try {
            state.iterationsLeft.await();
            Thread.sleep(1000);
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);

            Iterable<Float> latencies = state.getLatencies();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/inorderproducers/worker1-latencies.csv"))) {
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