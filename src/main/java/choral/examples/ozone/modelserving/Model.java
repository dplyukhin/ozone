package choral.examples.ozone.modelserving;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.AsyncChannel_B;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.ozone.modelserving.ConcurrentServing_Client;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Model {

    private static int modelID = -1;

    public static void debug(String msg) {
        Log.debug("MODEL" + modelID + ": " + msg);
    }

    public static void main(String[] args) {
        // Get the Model ID from the command line arg after --modelID
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--modelID")) {
                modelID = Integer.parseInt(args[i+1]);
            }
        }
        if (modelID == -1) {
            debug("Model ID not provided. Exiting.");
            return;
        }
        else if (modelID != 1 && modelID != 2) {
            debug("Invalid Model ID. Exiting.");
            return;
        }

        int BATCHER_FOR_MODEL = -1;
        int WORKER1_FOR_MODEL = -1;
        int WORKER2_FOR_MODEL = -1;
        if (modelID == 1) {
            BATCHER_FOR_MODEL = Config.BATCHER_FOR_MODEL1;
            WORKER1_FOR_MODEL = Config.WORKER1_FOR_MODEL1;
            WORKER2_FOR_MODEL = Config.WORKER2_FOR_MODEL1;
        }
        else if (modelID == 2) {
            BATCHER_FOR_MODEL = Config.BATCHER_FOR_MODEL2;
            WORKER1_FOR_MODEL = Config.WORKER1_FOR_MODEL2;
            WORKER2_FOR_MODEL = Config.WORKER2_FOR_MODEL2;
        }

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);

        debug("Connecting to other nodes...");

        AsyncChannel_B<Object> chB = new AsyncChannelImpl<>(
            threadPool,
            AsyncSocketChannel.connect(new JavaSerializer(), Config.HOST, BATCHER_FOR_MODEL)
        );
        debug("Connected to batcher.");
        SymChannel_B<Object> chW1 = AsyncSocketChannel.connect(
            new JavaSerializer(), Config.HOST, WORKER1_FOR_MODEL
        );
        SymChannel_B<Object> chW2 = AsyncSocketChannel.connect(
            new JavaSerializer(), Config.HOST, WORKER2_FOR_MODEL
        );
        debug("Connected to workers.");

        debug("Starting!");

        ModelState state = new ModelState();
        if (modelID == 1) {
            ConcurrentServing_Model1 prot = new ConcurrentServing_Model1(chB, chW1, chW2);
            for (int i = 0; i < Config.IMAGES_PER_CLIENT; i++)
                prot.onImage(state, new Token(i));
        }
        else if (modelID == 2) {
            ConcurrentServing_Model2 prot = new ConcurrentServing_Model2(chB, chW1, chW2);
            for (int i = 0; i < Config.IMAGES_PER_CLIENT; i++)
                prot.onImage(state, new Token(i));
        }

        threadPool.shutdownNow();
        debug("Done.");
    }
}
