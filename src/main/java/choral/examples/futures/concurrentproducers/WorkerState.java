package choral.examples.futures.concurrentproducers;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerState {
    String workerName;
    int waitTime;
    CountDownLatch iterationsLeft;
    ConcurrentHashMap<String, Long> startTimes;
    ConcurrentHashMap<String, Long> endTimes;

    public WorkerState(String workerName, int waitTime, int numIterations) {
        this.workerName = workerName;
        this.waitTime = waitTime;
        this.iterationsLeft = new CountDownLatch(numIterations);
        this.startTimes = new ConcurrentHashMap<>();
        this.endTimes = new ConcurrentHashMap<>();
    }

    public String produce(String input) {
        startTimes.put(input, System.currentTimeMillis());
        try {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e) {
            System.out.println("Worker wait interrupted!");
        }
        return input;
    }

    public void store(String x) {
        //System.out.println(workerName + " got responses: " + x);
        endTimes.put(x, System.currentTimeMillis());
        iterationsLeft.countDown();
    }
}