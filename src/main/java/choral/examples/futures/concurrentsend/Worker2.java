package choral.examples.futures.concurrentsend;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.Log;

import choral.examples.futures.concurrentsend.*;

public class Worker2 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannel_A<String>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER2_PORT
            )
        );

        Log.debug("Connection succeeded.");

        ConcurrentSend_Worker2 prot = new ConcurrentSend_Worker2();
        WorkerState state = new WorkerState("Worker2", 0);
        prot.go(ch, state, new Token(0));
    }
}
