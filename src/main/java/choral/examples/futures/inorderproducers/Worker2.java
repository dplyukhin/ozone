package choral.examples.futures.inorderproducers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import choral.Log;
import choral.channels.SymChannelImpl;
import choral.channels.SymChannel_A;
import choral.examples.futures.concurrentproducers.InOrderProducers_Worker2;
import choral.examples.futures.concurrentproducers.WorkerState;
import choral.runtime.DelayableChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannelImpl;
import choral.runtime.WrapperByteChannel.WrapperByteChannelImpl;

public class Worker2 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        SymChannel_A<Object> ch = 
            new DelayableChannel<Object>( 
                new SerializerChannelImpl(
                    new JavaSerializer(),
                    new WrapperByteChannelImpl(
                        SocketByteChannel.connect(
                            Server.HOST, Server.WORKER2_PORT
                        )
                    )
                ),
                Server.MAX_DELAY_MILLIS
            );

        Log.debug("Connection succeeded.");

        WorkerState state = new WorkerState("Worker2", 0, Server.NUM_ITERATIONS);
        InOrderProducers_Worker2 prot = new InOrderProducers_Worker2();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < Server.NUM_ITERATIONS; i++) {
            prot.go(ch, state, String.valueOf(i));
        }
        try {
            state.iterationsLeft.await();
            long endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);

            Iterable<Long> latencies = state.getLatencies();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("worker2-latencies.csv"))) {
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
