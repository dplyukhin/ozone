package choral.examples.ozone.concurrentsend;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import choral.Log;

public class WorkerState {
    String workerName;
    Random random;
    CountDownLatch latch;

    public WorkerState(String workerName, Integer numIterationsLeft) {
        this.workerName = workerName;
        this.random = new Random();
        this.latch = new CountDownLatch(numIterationsLeft);
    }

    public String compute(Integer input) {
        try {
            Thread.sleep(random.nextLong(Config.WORKER_MAX_COMPUTE_TIME_MILLIS));
        }
        catch (InterruptedException e) {
            Log.debug("Worker wait interrupted!");
        }
        latch.countDown();
        return "(data from " + workerName + ")";
    }

    public void await() {
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            Log.debug("Worker interrupted while waiting for all iterations to complete.");
        }
    }
}