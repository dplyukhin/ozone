package choral.examples.futures.inorderproducers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import choral.Log;
import choral.channels.SymChannel_A;
import choral.examples.futures.Scheduler;
import choral.examples.futures.concurrentproducers.InOrderProducers_Worker2;
import choral.examples.futures.concurrentproducers.WorkerState;
import choral.runtime.JavaSerializer;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.WrapperByteChannel.WrapperByteChannelImpl;

public class Worker2 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        SymChannel_A<Object> ch = 
            new SerializerChannel_A(
                new JavaSerializer(),
                new WrapperByteChannelImpl(
                    SocketByteChannel.connect(
                        Server.HOST, Server.WORKER2_PORT
                    )
                )
            );

        Log.debug("Connection succeeded.");

        WorkerState state = new WorkerState("Worker2", 0, Server.NUM_ITERATIONS);
        InOrderProducers_Worker2 prot = new InOrderProducers_Worker2();
        long startTime = System.currentTimeMillis();
        new Scheduler().schedule(
            i -> prot.go(ch, state, String.valueOf(i)), 
            Server.ITERATION_PERIOD_MILLIS,
            Server.NUM_ITERATIONS
        );
        try {
            state.iterationsLeft.await();
            Thread.sleep(1000);
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);

            Iterable<Float> latencies = state.getLatencies();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/inorderproducers/worker2-latencies.csv"))) {
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
