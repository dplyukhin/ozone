package choral.examples.futures.inordersend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import choral.Log;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.futures.concurrentsend.ConcurrentSend_Server;
import choral.examples.futures.concurrentsend.ServerState;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.JavaSerializer;

public class Server {
    public static final String HOST = "localhost";
    public static final int CLIENT_PORT = 8667;
    public static final int WORKER1_PORT = 8668;
    public static final int WORKER2_PORT = 8669;
    public static final int WORKER_MAX_COMPUTE_TIME_MILLIS = 10;
    public static final int NUM_ITERATIONS = 1000;

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");

        AsyncServerSocketChannel client_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(),
                Server.HOST, Server.CLIENT_PORT 
            );
		AsyncServerSocketChannel worker1_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(),
                Server.HOST, Server.WORKER1_PORT 
            );
		AsyncServerSocketChannel worker2_listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(),
                Server.HOST, Server.WORKER2_PORT 
            );

        SymChannel_A<Object> ch_c = client_listener.getNext();
        Log.debug("Client connected.");

        SymChannel_B<Object> ch_w1 = worker1_listener.getNext();
        Log.debug("Worker1 connected.");

        SymChannel_B<Object> ch_w2 = worker2_listener.getNext();
        Log.debug("Worker2 connected.");

        ConcurrentSend_Server prot = new ConcurrentSend_Server();
        ServerState state = new ServerState();
        long startTime = System.nanoTime();
        for (int i = 0; i < NUM_ITERATIONS; i++)
            prot.inorderFetchAndForward(ch_w1, ch_w2, ch_c, state, i);

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1000000);

        Iterable<Float> keyLatencies = state.getKeyLatencies();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/inordersend/key-latencies.csv"))) {
            for (float value : keyLatencies) {
                writer.write(Float.toString(value));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterable<Float> txtLatencies = state.getTxtLatencies();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data/inordersend/txt-latencies.csv"))) {
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
