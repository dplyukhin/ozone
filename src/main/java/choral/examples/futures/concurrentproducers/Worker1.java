package choral.examples.futures.concurrentproducers;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;

public class Worker1 {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannel_A<String>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER1_PORT
            )
        );

        System.out.println("Connection succeeded.");

        ConcurrentProducers_Worker1 prot = new ConcurrentProducers_Worker1();
        WorkerState state = new WorkerState("Worker1", 0);
        prot.go(ch, state, new Token(0));
    }
}