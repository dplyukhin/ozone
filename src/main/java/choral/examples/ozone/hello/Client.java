package choral.examples.ozone.hello;

import java.util.concurrent.Executors;

import choral.Log;
import choral.channels.AsyncChannel_A;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Client {

    public static void main(String[] args) {
        Log.debug("Connecting to server...");

        AsyncChannel_A<String> ch = new AsyncChannelImpl<>(
            Executors.newSingleThreadScheduledExecutor(),
            AsyncSocketChannel.connect( 
                new JavaSerializer(),
                Config.HOST, Config.PORT
            )
        );

        Log.debug("Connection succeeded.");

        HelloRoles_Client prot = new HelloRoles_Client();
        prot.sayHello(ch, "Hiya!", new Token(0));

        Log.debug("Done.");
    }
}
