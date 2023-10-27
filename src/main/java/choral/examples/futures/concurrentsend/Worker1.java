package choral.examples.futures.concurrentsend;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;

import choral.examples.futures.concurrentsend.*;

public class Worker1 {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannel_A<String>(
            Executors.newSingleThreadExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER1_PORT
            )
        );

        System.out.println("Connection succeeded.");

        ConcurrentSend_Worker1 prot = new ConcurrentSend_Worker1();
        WorkerState state = new WorkerState("Worker1", 5000);
        prot.go(ch, state, new Token(0));
    }
}
