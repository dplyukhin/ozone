package choral.examples.ozone.concurrentsend;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(4);
        AsyncChannel_A<Object> ch = new AsyncChannelImpl<Object>(
            threadPool,
            AsyncSocketChannel.connect( 
                new JavaSerializer(), Config.SERVER_HOST, Config.WORKER1_PORT
            )
        );

        Log.debug("Connection succeeded.");

        ConcurrentSend_KeyService prot = new ConcurrentSend_KeyService();
        WorkerState state = new WorkerState("Worker1", Config.NUM_ITERATIONS);
        for (int i = 0; i < Config.NUM_ITERATIONS; i++)
            prot.concurrentFetchAndForward(ch, state, new Token(i));

        state.await();
        try {
            Thread.sleep(20000); // Give worker time to send final message
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPool.shutdownNow();
        Log.debug("Worker 1 done.");
    }
}
