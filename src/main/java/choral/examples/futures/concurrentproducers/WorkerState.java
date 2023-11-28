package choral.examples.futures.concurrentsend;

import java.lang.Thread;

public class WorkerState {
    String workerName;
    int waitTime;

    public WorkerState(String workerName, int waitTime) {
        this.workerName = workerName;
        this.waitTime = waitTime;
    }

    public String produce() {
        try {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e) {
            System.out.println("Worker wait interrupted!");
        }
        return "(data from " + workerName + ")";
    }

    public void store(String x) {}
}