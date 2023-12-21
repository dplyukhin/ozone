package choral.examples.futures;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Scheduler {

    ScheduledFuture<?> scheduledFuture;

    public void schedule(Consumer<Integer> f, int periodMillis, int totalIterations) {
        // The number of iterations that have been completed so far
        AtomicInteger iteration = new AtomicInteger(0);
        // The start time of the most recent iteration - or 0 if no iterations have been completed yet
        AtomicLong lastRunTime = new AtomicLong(0);

        /** A task that runs the function */
        Runnable task = new Runnable() {
            public long currentTimeMillis() {
                return System.nanoTime() / 1000000L;
            }

            /** Check if the current time is within 3 milliseconds of the start of a period */
            public boolean isWithinTimeWindow(long now) {
                return now % periodMillis < 15 || now % periodMillis > periodMillis - 15;
            }

            /** Whether the function has been invoked in the current time window */
            public boolean hasRunYet(long now) {
                if (lastRunTime.get() == 0) {
                    return false;
                }
                return Math.abs(lastRunTime.get() - now) <= 15;
            }

            public void run() {
                long now = currentTimeMillis();
                if (isWithinTimeWindow(now)) {
                    if (!hasRunYet(now)) {
                        int i = iteration.incrementAndGet();
                        lastRunTime.set(now);
                        f.accept(i);
                        if (i >= totalIterations) {
                            scheduledFuture.cancel(true);
                        }
                    }
                }
            }
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        this.scheduledFuture = scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.MILLISECONDS);
    }
    
}
