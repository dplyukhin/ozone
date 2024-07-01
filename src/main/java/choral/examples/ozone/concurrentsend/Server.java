package choral.examples.ozone.concurrentsend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.AsyncChannel_B;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Server {

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

		AsyncServerSocketChannel client_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(), Config.SERVER_HOST, Config.CLIENT_PORT
            );
		AsyncServerSocketChannel worker1_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(), Config.SERVER_HOST, Config.WORKER1_PORT
            );
		AsyncServerSocketChannel worker2_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(), Config.SERVER_HOST, Config.WORKER2_PORT
            );

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);

        AsyncChannel_A<Object> ch_c = new AsyncChannelImpl<Object>( 
            threadPool, client_listener.getNext()
        );
        Log.debug("Client connected.");

        AsyncChannel_B<Object> ch_w1 = new AsyncChannelImpl<Object>( 
            threadPool, worker1_listener.getNext()
        );
        Log.debug("Worker1 connected.");

        AsyncChannel_B<Object> ch_w2 = new AsyncChannelImpl<Object>( 
            threadPool, worker2_listener.getNext()
        );
        Log.debug("Worker2 connected.");

        ConcurrentSend_Server prot = new ConcurrentSend_Server();
        ServerState state = new ServerState();
        long startTime = System.nanoTime();
        for (int i = 0; i < Config.NUM_ITERATIONS; i++) {
            prot.concurrentFetchAndForward(ch_w1, ch_w2, ch_c, state, i, new Token(i));
        }

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
        worker1_listener.close();
        worker2_listener.close();
        threadPool.shutdownNow();
        Log.debug("Server done.");
    }
}
