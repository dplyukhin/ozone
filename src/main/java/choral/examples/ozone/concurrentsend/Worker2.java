package choral.examples.ozone.concurrentsend;

import java.util.concurrent.Executors;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Worker2 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<Object> ch = new AsyncChannelImpl<Object>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketChannel.connect( 
                new JavaSerializer(),
                Config.HOST, Config.WORKER2_PORT
            )
        );

        Log.debug("Connection succeeded.");

        ConcurrentSend_ContentService prot = new ConcurrentSend_ContentService();
        WorkerState state = new WorkerState("Worker2");
        for (int i = 0; i < Config.NUM_ITERATIONS; i++)
            prot.concurrentFetchAndForward(ch, state, new Token(i));
    }
}
