package choral.examples.futures;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Scheduler {

    ScheduledFuture<?> scheduledFuture;

    public void schedule(Consumer<Integer> f, int periodMillis, int totalIterations) {
        AtomicInteger iteration = new AtomicInteger(0);

        /** A task that runs the function */
        Runnable task = new Runnable() {
            public void run() {
                int i = iteration.getAndIncrement();
                f.accept(i);
                if (i >= totalIterations) {
                    scheduledFuture.cancel(true);
                }
            }
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        this.scheduledFuture = scheduler.scheduleAtFixedRate(task, 0, periodMillis, TimeUnit.MILLISECONDS);
    }
    
}
