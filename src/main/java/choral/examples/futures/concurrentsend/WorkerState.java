package choral.examples.futures.concurrentsend;

import java.lang.Thread;
import choral.Log;

public class WorkerState {
    String workerName;
    int waitTime;

    public WorkerState(String workerName, int waitTime) {
        this.workerName = workerName;
        this.waitTime = waitTime;
    }

    public String compute() {
        try {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e) {
            Log.debug("Worker wait interrupted!");
        }
        return "(data from " + workerName + ")";
    }
}