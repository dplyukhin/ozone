package choral.examples.futures.inorderproducers;

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
import choral.runtime.DelayableChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.runtime.Media.SocketByteChannel;
import choral.Log;
import choral.channels.SymChannel_A;

import choral.examples.futures.concurrentproducers.WorkerState;
import choral.examples.futures.concurrentproducers.ServerState;
import choral.examples.futures.concurrentproducers.InOrderProducers_Worker1;
import choral.examples.futures.concurrentproducers.InOrderProducers_Worker2;
import choral.examples.futures.concurrentproducers.InOrderProducers_Server;

public class Worker2 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        SymChannel_A<String> ch = new DelayableChannel<String>(
            SocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER2_PORT
            ),
            Server.MAX_DELAY_MILLIS
        );

        Log.debug("Connection succeeded.");

        WorkerState state = new WorkerState("Worker2", 0, Server.NUM_ITERATIONS);
        InOrderProducers_Worker2 prot = new InOrderProducers_Worker2();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < Server.NUM_ITERATIONS; i++) {
            prot.go(ch, state, String.valueOf(i), new Token(i));
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
