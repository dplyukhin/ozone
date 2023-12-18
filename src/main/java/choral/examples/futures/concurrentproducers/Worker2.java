package choral.examples.futures.concurrentproducers;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;

public class Worker2 {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannel_A<String>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.WORKER2_PORT
            )
        );

        System.out.println("Connection succeeded.");

        WorkerState state = new WorkerState("Worker2", 0);
        ConcurrentProducers_Worker2 prot = new ConcurrentProducers_Worker2();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < Server.NUM_ITERATIONS; i++) {
            prot.go(ch, state, new Token(i));
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
    }
}
