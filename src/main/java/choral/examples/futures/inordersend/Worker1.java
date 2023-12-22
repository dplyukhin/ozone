package choral.examples.futures.inordersend;

import choral.Log;
import choral.channels.SymChannel_A;
import choral.examples.futures.concurrentsend.ConcurrentSend_KeyService;
import choral.examples.futures.concurrentsend.WorkerState;
import choral.runtime.JavaSerializer;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.WrapperByteChannel.WrapperByteChannelImpl;

public class Worker1 {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        SymChannel_A<Object> ch = 
            new SerializerChannel_A(
                new JavaSerializer(),
                new WrapperByteChannelImpl(
                    SocketByteChannel.connect(
                        Server.HOST, Server.WORKER1_PORT
                    )
                )
            );

        Log.debug("Connection succeeded.");

        ConcurrentSend_KeyService prot = new ConcurrentSend_KeyService();
        WorkerState state = new WorkerState("Worker1");
        for (int i = 0; i < Server.NUM_ITERATIONS; i++)
            prot.inorderFetchAndForward(ch, state);
    }
}
