package choral.examples.ozone.modelservingakka;

import java.io.IOException;

import choral.Log;
import choral.examples.ozone.modelserving.WorkerState;

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

        debug("Starting!");

        WorkerState state = new WorkerState();
        if (workerID == 1) {

        }
        else if (workerID == 2) {

        }
    }
}
