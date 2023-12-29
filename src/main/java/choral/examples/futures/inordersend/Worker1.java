package choral.examples.futures.inordersend;

import choral.Log;
import choral.channels.SymChannel_A;
import choral.examples.futures.concurrentsend.ConcurrentSend_KeyService;
import choral.examples.futures.concurrentsend.WorkerState;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        SymChannel_A<Object> ch = 
            AsyncSocketChannel.connect(
                new JavaSerializer(),
                Server.HOST, Server.WORKER1_PORT
            );

        Log.debug("Connection succeeded.");

        ConcurrentSend_KeyService prot = new ConcurrentSend_KeyService();
        WorkerState state = new WorkerState("Worker1");
        for (int i = 0; i < Server.NUM_ITERATIONS; i++)
            prot.inorderFetchAndForward(ch, state);
    }
}
