package choral.examples.futures.concurrentproducers;

import java.lang.Thread;
import choral.Log;

public class ServerState {
    int waitTime;

    public ServerState(int waitTime) {
        this.waitTime = waitTime;
    }

    public String compute(String input) {
        Log.debug("Server processing the following input: " + input + "...");
        try {
            Thread.sleep(waitTime);
            Log.debug("Done processing " + input + ".");
        }
        catch (InterruptedException e) {
            Log.debug("Server wait interrupted!");
        }
        return input;
    }
}