package choral.examples.ozone.simpletimeout;

import java.util.concurrent.Executors;

import choral.Log;
import choral.channels.AsyncChannel_B;
import choral.runtime.AsyncServerSocketChannel;
import choral.runtime.JavaSerializer;
import choral.runtime.Token;
import choral.runtime.UnreliableAsyncChannel;

public class Server {

    public static void main(String[] args) throws java.io.IOException {
        Log.debug("Running server...");
        // Run server and create a channel from the first open connection
		AsyncServerSocketChannel listener =
            AsyncServerSocketChannel.at( 
                new JavaSerializer(), 
                Config.HOST, Config.PORT
            );

        AsyncChannel_B<String> ch = new UnreliableAsyncChannel<String>( 
            Executors.newScheduledThreadPool(2),
            listener.getNext()
        );
        Log.debug("Client connected.");

        SimpleTimeout_Server prot = new SimpleTimeout_Server();
        prot.go(ch, new Token(0));

        listener.close();
        Log.debug("Done.");
    }
}
