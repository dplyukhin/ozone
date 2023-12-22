package choral.examples.futures.concurrentsend;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.Log;

import choral.examples.futures.concurrentsend.*;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<Object> ch = new AsyncChannelImpl<Object>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER1_PORT
            )
        );

        Log.debug("Connection succeeded.");

        ConcurrentSend_KeyService prot = new ConcurrentSend_KeyService();
        WorkerState state = new WorkerState("Worker1");
        prot.concurrentFetchAndForward(ch, state, new Token(0));
    }
}
