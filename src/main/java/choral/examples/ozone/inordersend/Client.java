package choral.examples.ozone.inordersend;

import choral.Log;
import choral.channels.SymChannel_B;
import choral.examples.ozone.concurrentsend.ConcurrentSend_Client;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        SymChannel_B<Object> ch = 
            AsyncSocketChannel.connect(
                new JavaSerializer(),
                Config.HOST, Config.CLIENT_PORT
            );
        Log.debug("Connection succeeded.");

        ConcurrentSend_Client prot = new ConcurrentSend_Client();
        for (int i = 0; i < Config.NUM_ITERATIONS; i++)
            prot.inorderFetchAndForward(ch);
    }
}
