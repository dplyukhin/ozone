package choral.examples.futures.concurrentsend;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import choral.runtime.AsyncChannel;
import choral.runtime.Token;

public class ConcurrentSend@( KeyService, ContentService, Server, Client ) {
	public void go( 
      AsyncChannel@( KeyService, Server )< String > ch1, 
      AsyncChannel@( ContentService, Server )< String > ch2, 
      AsyncChannel@( Server, Client )< String > ch3, 
      WorkerState@KeyService state_ks,
      WorkerState@ContentService state_cs,
      Token@KeyService tok_ks,
      Token@ContentService tok_cs,
      Token@Server tok_s,
      Token@Client tok_c
   ) { 
      // Services compute data.
      String@KeyService key = state_ks.compute();
      String@ContentService txt = state_cs.compute();

      // Services send data to the server.
      CompletableFuture@Server<String> f1 = ch1.<String>com( key, 1@KeyService, tok_ks, 1@Server, tok_s );
      CompletableFuture@Server<String> f2 = ch2.<String>com( txt, 2@ContentService, tok_cs, 2@Server, tok_s );

      // Server forwards data to the client.
      CompletableFuture@Client<String> f3 = ch3.<String>com( f1, 3@Server, tok_s, 3@Client, tok_c );
      CompletableFuture@Client<String> f4 = ch3.<String>com( f2, 4@Server, tok_s, 4@Client, tok_c );

      // Client handles the data.
      Consumer@Client<String> onData1 = new OnData@Client("KeyService"@Client);
      f3.thenAccept(onData1);
      Consumer@Client<String> onData2 = new OnData@Client("ContentService"@Client);
      f4.thenAccept(onData2);
   }
}