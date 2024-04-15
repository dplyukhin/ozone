package choral.examples.ozone.modelserving;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.ozone.modelserving.ModelServing_Client;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Client {

    public static void debug(String msg) {
        Log.debug("CLIENT: " + msg);
    }

    public static void main(String[] args) {

        // Read the image filename from args
        if (args.length < 1) {
            debug("Image filename not provided. Exiting.");
            return;
        }
        String filePath = args[0];

        Image img = null;
        try {
            byte[] imgBytes = Files.readAllBytes(Paths.get(filePath));
            img = new Image(imgBytes);
        } 
        catch (IOException e) {
            debug("Couldn't find image " + filePath + ". Exiting.");
            return;
        }

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);

        debug("Connecting to other nodes...");

        AsyncServerSocketChannel worker1_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.CLIENT_FOR_WORKER1
        );
        AsyncServerSocketChannel worker2_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.CLIENT_FOR_WORKER2
        );
        AsyncServerSocketChannel batcher_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, Config.CLIENT_FOR_BATCHER
        );

        try {
            SymChannel_A<Object> chW1 = worker1_listener.getNext();
            debug("Worker 1 connected.");
            SymChannel_A<Object> chW2 = worker2_listener.getNext();
            debug("Worker 2 connected.");
            AsyncChannel_A<Object> chB = new AsyncChannelImpl<Object>( 
                threadPool, batcher_listener.getNext()
            );
            debug("Batcher connected.");

            chB.select();

            debug("Client starting!");

            ModelServing_Client prot = new ModelServing_Client(chW1, chW2, chB);
            ClientState state = new ClientState();

            prot.onImage(img, 0, state, new Token(0));
            //for (int i = 0; i < Config.NUM_ITERATIONS; i++)
            //    prot.on

        } 
        catch (IOException e) {
            debug("Error in client, aborting: " + e.getMessage());
        }
        finally {
            threadPool.shutdownNow();
            worker1_listener.close();
            worker2_listener.close();
            batcher_listener.close();
            debug("Client done.");
        }
    }
}
