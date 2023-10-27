package choral.examples.futures.playground;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import choral.lang.Unit;
import choral.runtime.AsyncChannel_A;
import choral.runtime.AsyncSocketByteChannel;
import choral.runtime.DataMsg;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.Token;
import choral.runtime.IntegrityKey;

public class Client {

    public static void main(String[] args) {
        System.out.println("Connecting to server...");

        AsyncSocketByteChannel ch = 
            //new AsyncChannel_A<Object>(
            //Executors.newSingleThreadExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.PORT
            );
        //);

        ch.com(new IntegrityKey(0, new Token(0)));
        System.out.println("Sent hello");
        System.out.println("Got: " + ch.com());
    }
}
