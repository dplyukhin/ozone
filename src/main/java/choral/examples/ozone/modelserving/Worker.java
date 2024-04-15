package choral.examples.ozone.modelserving;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.channels.AsyncChannel_B;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.ozone.modelserving.ModelServing_Client;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Worker {

    private static int workerID = -1;

    public static void debug(String msg) {
        Log.debug("WORKER" + workerID + ": " + msg);
    }

    public static void main(String[] args) {
        // Get the Worker ID from the command line arg after --workerID
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--workerID")) {
                workerID = Integer.parseInt(args[i+1]);
            }
        }
        if (workerID == -1) {
            debug("Worker ID not provided. Exiting.");
            return;
        }
        else if (workerID != 1 && workerID != 2) {
            debug("Invalid Worker ID. Exiting.");
            return;
        }

        int CLIENT_FOR_WORKER = -1;
        int BATCHER_FOR_WORKER = -1;
        int WORKER_FOR_MODEL1 = -1;
        int WORKER_FOR_MODEL2 = -1;
        if (workerID == 1) {
            CLIENT_FOR_WORKER = Config.CLIENT_FOR_WORKER1;
            BATCHER_FOR_WORKER = Config.BATCHER_FOR_WORKER1;
            WORKER_FOR_MODEL1 = Config.WORKER1_FOR_MODEL1;
            WORKER_FOR_MODEL2 = Config.WORKER1_FOR_MODEL2;
        }
        else if (workerID == 2) {
            CLIENT_FOR_WORKER = Config.CLIENT_FOR_WORKER2;
            BATCHER_FOR_WORKER = Config.BATCHER_FOR_WORKER2;
            WORKER_FOR_MODEL1 = Config.WORKER2_FOR_MODEL1;
            WORKER_FOR_MODEL2 = Config.WORKER2_FOR_MODEL2;
        }

        debug("Connecting to other nodes...");

        SymChannel_B<Object> chC = AsyncSocketChannel.connect(
            new JavaSerializer(), Config.HOST, CLIENT_FOR_WORKER
        );
        debug("Connected to client.");

        SymChannel_B<Object> chB = AsyncSocketChannel.connect(
            new JavaSerializer(), Config.HOST, BATCHER_FOR_WORKER
        );
        debug("Connected to batcher.");

        AsyncServerSocketChannel model1_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, WORKER_FOR_MODEL1
        );
        AsyncServerSocketChannel model2_listener = AsyncServerSocketChannel.at( 
            new JavaSerializer(), Config.HOST, WORKER_FOR_MODEL2
        );

        try {
            SymChannel_A<Object> chM1 = model1_listener.getNext();
            SymChannel_A<Object> chM2 = model2_listener.getNext();
            debug("Models connected.");

            chB.<BenchmarkReady>select(BenchmarkReady.READY);

            debug("Starting!");

            WorkerState state = new WorkerState();
            if (workerID == 1) {
                ModelServing_Worker1 prot = new ModelServing_Worker1(chC, chB, chM1, chM2);
                prot.onImage(state, new Token(0));
                //for (int i = 0; i < Config.NUM_ITERATIONS; i++)
                //    prot.on
            }
            else if (workerID == 2) {
                ModelServing_Worker2 prot = new ModelServing_Worker2(chC, chB, chM1, chM2);
                prot.onImage(state, new Token(0));
                //for (int i = 0; i < Config.NUM_ITERATIONS; i++)
                //    prot.on
            }

        } 
        catch (IOException e) {
            debug("Aborting: " + e.getMessage());
        }
        finally {
            model1_listener.close();
            model2_listener.close();
            debug("Done.");
        }
    }
}
