package choral.examples.ozone.hello;

import java.util.concurrent.Executors;

import choral.Log;
import choral.channels.AsyncChannel_B;
import choral.runtime.AsyncChannelImpl;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;

public class Server {

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");
        // Run server and create a channel from the first open connection
		AsyncServerSocketChannel listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(),
                Config.HOST, Config.PORT
            );

        AsyncChannel_B<String> ch = new AsyncChannelImpl<>(
            Executors.newSingleThreadScheduledExecutor(),
            listener.getNext()
        );
        Log.debug("Client connected.");

        HelloRoles_Server prot = new HelloRoles_Server();
        prot.sayHello(ch, new Token(0));

        listener.close();
        Log.debug("Done.");
    }
}
