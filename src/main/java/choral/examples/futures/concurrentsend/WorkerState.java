package choral.examples.futures.concurrentsend;

import java.lang.Thread;
import choral.Log;

public class WorkerState {
    String workerName;

    public WorkerState(String workerName) {
        this.workerName = workerName;
    }

    public String compute(Integer input) {
        try {
            Thread.sleep(Server.WORKER_MAX_COMPUTE_TIME_MILLIS);
        }
        catch (InterruptedException e) {
            Log.debug("Worker wait interrupted!");
        }
        return "(data from " + workerName + ")";
    }
}