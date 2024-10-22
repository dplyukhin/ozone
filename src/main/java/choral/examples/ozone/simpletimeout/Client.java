package choral.examples.ozone.simpletimeout;

import java.util.concurrent.Executors;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;
import choral.runtime.UnreliableAsyncChannel;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new UnreliableAsyncChannel<String>(
            Executors.newScheduledThreadPool(2),
            AsyncSocketChannel.connect( 
                new JavaSerializer(),
                Config.HOST, Config.PORT
            )
        );

        Log.debug("Connection succeeded.");

        SimpleTimeout_Client prot = new SimpleTimeout_Client();
        prot.go(ch, new Token(0));

        Log.debug("Done.");
    }
}
