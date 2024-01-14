package choral.examples.ozone.concurrentsend;

import java.util.Random;

import choral.Log;

public class WorkerState {
    String workerName;
    Random random;

    public WorkerState(String workerName) {
        this.workerName = workerName;
        this.random = new Random();
    }

    public String compute(Integer input) {
        try {
            Thread.sleep(random.nextLong(Config.WORKER_MAX_COMPUTE_TIME_MILLIS));
        }
        catch (InterruptedException e) {
            Log.debug("Worker wait interrupted!");
        }
        return "(data from " + workerName + ")";
    }
}