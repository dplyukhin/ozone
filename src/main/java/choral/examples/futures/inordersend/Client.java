package choral.examples.futures.inordersend;

import choral.Log;
import choral.channels.SymChannel_B;
import choral.examples.futures.concurrentsend.ConcurrentSend_Client;
import choral.runtime.JavaSerializer;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.WrapperByteChannel.WrapperByteChannelImpl;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        SymChannel_B<Object> ch = 
            new SerializerChannel_B(
                new JavaSerializer(),
                new WrapperByteChannelImpl(
                    SocketByteChannel.connect(
                        Server.HOST, Server.CLIENT_PORT
                    )
                )
            );

        Log.debug("Connection succeeded.");

        ConcurrentSend_Client prot = new ConcurrentSend_Client();
        for (int i = 0; i < Server.NUM_ITERATIONS; i++)
            prot.inorderFetchAndForward(ch);
    }
}
