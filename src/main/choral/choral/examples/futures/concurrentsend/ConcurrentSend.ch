package choral.examples.futures.hello;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import choral.runtime.AsyncChannel;
import choral.runtime.Token;

public class ConcurrentSend@( Worker1, Worker2, Server, Client ) {
	public void go( 
      AsyncChannel@( Worker1, Server )< String > ch1, 
      AsyncChannel@( Worker2, Server )< String > ch2, 
      AsyncChannel@( Server, Client )< String > ch3, 
      WorkerState@Worker1 state1,
      WorkerState@Worker2 state2,
      Token@Worker1 tok1,
      Token@Worker2 tok2,
      Token@Server tok_s,
      Token@Client tok_c
   ) { 
      // Workers compute data.
      String@Worker1 str1 = state1.compute();
      String@Worker2 str2 = state2.compute();

      // Workers send data to the server.
      CompletableFuture@Server<String> f1 = ch1.<String>com( str1, 1@Worker1, tok1, 1@Server, tok_s );
      CompletableFuture@Server<String> f2 = ch2.<String>com( str2, 2@Worker2, tok2, 2@Server, tok_s );

      // Server forwards data to the client.
      CompletableFuture@Client<String> f3 = ch3.<String>com( f1, 3@Server, tok_s, 3@Client, tok_c );
      CompletableFuture@Client<String> f4 = ch3.<String>com( f2, 4@Server, tok_s, 4@Client, tok_c );

      // Client handles the data.
      Consumer@Client<String> onData1 = new OnData@Client("Worker 1"@Client);
      f3.thenAccept(onData1);
      Consumer@Client<String> onData2 = new OnData@Client("Worker 2"@Client);
      f3.thenAccept(onData2);
   }
}