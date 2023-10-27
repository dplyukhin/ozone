package choral.examples.futures.concurrentsend;

import java.util.concurrent.Executors;

import choral.runtime.AsyncChannel_B;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;

import choral.examples.futures.concurrentsend.*;

public class Client {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        AsyncChannel_B<String> ch = new AsyncChannel_B<String>(
            Executors.newSingleThreadExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.CLIENT_PORT
            )
        );

        System.out.println("Connection succeeded.");

        ConcurrentSend_Client prot = new ConcurrentSend_Client();
        prot.go(ch, new Token(0));
    }
}
