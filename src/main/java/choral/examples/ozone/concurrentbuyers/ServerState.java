package choral.examples.ozone.concurrentbuyers;

import choral.Log;

public class ServerState {
    int waitTime;

    public ServerState(int waitTime) {
        this.waitTime = waitTime;
    }

    public String sell(String itemID) {
        try {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e) {
            Log.debug("Server wait interrupted!");
        }
        return "(item with ID " + itemID + ")";
    }
}