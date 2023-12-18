package choral.examples.futures.concurrentproducers;

import java.lang.Thread;

public class ServerState {
    int waitTime;

    public ServerState(int waitTime) {
        this.waitTime = waitTime;
    }

    public String compute(String input) {
        System.out.println("Server processing the following input: " + input + "...");
        try {
            Thread.sleep(waitTime);
            System.out.println("Done processing " + input + ".");
        }
        catch (InterruptedException e) {
            System.out.println("Server wait interrupted!");
        }
        return input;
    }
}