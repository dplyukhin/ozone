package choral.examples.futures.concurrentsend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import choral.Log;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncServerSocketByteChannel;
import choral.runtime.Token;
import choral.runtime.Serializers.KryoSerializer;

public class Server {
    public static final String HOST = "localhost";
    public static final int CLIENT_PORT = 8667;
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int WORKER_MAX_COMPUTE_TIME_MILLIS = 10;
    public static final int NUM_ITERATIONS = 2000;

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

		AsyncServerSocketByteChannel client_listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.CLIENT_PORT 
            );
		AsyncServerSocketByteChannel worker1_listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.WORKER1_PORT 
            );
		AsyncServerSocketByteChannel worker2_listener =
            AsyncServerSocketByteChannel.at( 
                KryoSerializer.getInstance(), 
                Server.HOST, Server.WORKER2_PORT 
            );

        AsyncChannel_A<Object> ch_c = new AsyncChannelImpl<Object>( 
            Executors.newSingleThreadScheduledExecutor(),
            client_listener.getNext()
        );
        Log.debug("Client connected.");

        AsyncChannel_B<Object> ch_w1 = new AsyncChannelImpl<Object>( 
            Executors.newSingleThreadScheduledExecutor(),
            worker1_listener.getNext()
        );
        Log.debug("Worker1 connected.");

        AsyncChannel_B<Object> ch_w2 = new AsyncChannelImpl<Object>( 
            Executors.newSingleThreadScheduledExecutor(),
            worker2_listener.getNext()
        );
        Log.debug("Worker2 connected.");

        ConcurrentSend_Server prot = new ConcurrentSend_Server();
        ServerState state = new ServerState();
        long startTime = System.nanoTime();
        for (int i = 0; i < NUM_ITERATIONS; i++)
            prot.concurrentFetchAndForward(ch_w1, ch_w2, ch_c, state, i, new Token(i));

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1000000);

        Iterable<Float> keyLatencies = state.getKeyLatencies();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/concurrentsend/key-latencies.csv"))) {
            for (float value : keyLatencies) {
                writer.write(Float.toString(value));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterable<Float> txtLatencies = state.getTxtLatencies();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/concurrentsend/txt-latencies.csv"))) {
            for (float value : txtLatencies) {
                writer.write(Float.toString(value));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        client_listener.close();
        Log.debug("Done.");
    }
}
