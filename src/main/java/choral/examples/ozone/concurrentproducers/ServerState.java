package choral.examples.ozone.concurrentproducers;

import java.util.Random;

import choral.Log;

public class ServerState {
    Random random;

    public ServerState() {
        this.random = new Random();
    }

    public String compute(String input) {
        Log.debug("Server processing the following input: " + input + "...");
        try {
            Thread.sleep(Config.SERVER_MAX_COMPUTE_TIME_MILLIS);
            Log.debug("Done processing " + input + ".");
        }
        catch (InterruptedException e) {
            Log.debug("Server wait interrupted!");
        }
        return input;
    }
}
