package choral.examples.futures.concurrentsend;

import java.lang.Thread;

public class ServerState {
    int waitTime;

    public ServerState(int waitTime) {
        this.waitTime = waitTime;
    }

    public String compute(String input) {
        try {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e) {
            System.out.println("Server wait interrupted!");
        }
        return "(data processed by server; input: " + input + ")";
    }
}