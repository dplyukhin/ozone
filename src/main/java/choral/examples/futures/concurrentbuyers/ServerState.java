package choral.examples.futures.concurrentbuyers;

import java.lang.Thread;

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
            System.out.println("Server wait interrupted!");
        }
        return "(item with ID " + itemID + ")";
    }
}