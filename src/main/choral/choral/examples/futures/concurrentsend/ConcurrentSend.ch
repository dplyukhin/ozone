package choral.examples.futures.concurrentsend;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

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
      String@KeyService key = state_ks.compute( 0@KeyService );
      String@ContentService txt = state_cs.compute( 0@ContentService );

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

	public void concurrentFetchAndForward( 
      AsyncChannel@( KeyService, Server )< Object > ch1, 
      AsyncChannel@( ContentService, Server )< Object > ch2, 
      AsyncChannel@( Server, Client )< Object > ch3, 
      WorkerState@KeyService state_ks,
      WorkerState@ContentService state_cs,
      Integer@Server input,
      Token@KeyService tok_ks,
      Token@ContentService tok_cs,
      Token@Server tok_s,
      Token@Client tok_c
   ) {
      // Server sends the input to KS and CS.
      CompletableFuture@KeyService<Integer> input_ks = ch1.<Integer>com( input, 1@Server, tok_s, 1@KeyService, tok_ks );
      CompletableFuture@ContentService<Integer> input_cs = ch2.<Integer>com( input, 2@Server, tok_s, 2@ContentService, tok_cs );

      // Services compute data.
      CompletableFuture@KeyService<String> key = input_ks.<String>thenApply( new OnInput@KeyService(state_ks) );
      CompletableFuture@ContentService<String> txt = input_cs.<String>thenApply( new OnInput@ContentService(state_cs) );

      // Services send data to the server.
      CompletableFuture@Server<String> f1 = ch1.<String>com( key, 3@KeyService, tok_ks, 3@Server, tok_s );
      CompletableFuture@Server<String> f2 = ch2.<String>com( txt, 4@ContentService, tok_cs, 4@Server, tok_s );

      // Server forwards data to the client.
      CompletableFuture@Client<String> f3 = ch3.<String>com( f1, 5@Server, tok_s, 5@Client, tok_c );
      CompletableFuture@Client<String> f4 = ch3.<String>com( f2, 6@Server, tok_s, 6@Client, tok_c );

      // Client handles the data.
      CompletableFuture@Client<Void> acceptedKey = f3.thenAccept( new OnClientKey@Client() );
      CompletableFuture@Client<Void> acceptedTxt = f4.thenAccept( new OnClientTxt@Client() );

      // Client sends back acknowledgments.
      CompletableFuture@Server<Void> f5 = ch3.<Void>com( acceptedKey, 7@Client, tok_c, 7@Server, tok_s );
      CompletableFuture@Server<Void> f6 = ch3.<Void>com( acceptedTxt, 8@Client, tok_c, 8@Server, tok_s );

      // Server handles acknowledgments.
      f5.thenAccept( new OnKeyAck@Server() );
      f6.thenAccept( new OnTxtAck@Server() );

      f5.join();
      f6.join();
   }
}