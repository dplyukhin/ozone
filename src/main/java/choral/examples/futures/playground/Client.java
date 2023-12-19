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
import choral.Log;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncSocketByteChannel ch = 
            //new AsyncChannel_A<Object>(
            //Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketByteChannel.connect( 
                KryoSerializer.getInstance(),
                Server.HOST, Server.PORT
            );
        //);

        ch.com(new IntegrityKey(0, new Token(0)));
        Log.debug("Sent hello");
        Log.debug("Got: " + ch.com());
    }
}
