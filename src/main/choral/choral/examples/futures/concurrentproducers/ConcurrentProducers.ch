package choral.examples.futures.concurrentproducers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import choral.runtime.AsyncChannel;
import choral.runtime.Token;

public class ConcurrentProducers@( Worker1, Worker2, Server ) {
	public void go( 
      AsyncChannel@( Worker1, Server )< String > ch1, 
      AsyncChannel@( Worker2, Server )< String > ch2, 
      WorkerState@Worker1 state1,
      WorkerState@Worker2 state2,
      ServerState@Server state_s,
      Token@Worker1 tok1,
      Token@Worker2 tok2,
      Token@Server tok_s,
   ) { 
      // Workers produce data.
      String@Worker1 x1 = state1.produce();
      String@Worker2 x2 = state2.produce();

      // Workers send data to the server.
      CompletableFuture@Server<String> f_x1 = ch1.<String>com( x1, 1@Worker1, tok1, 1@Server, tok_s );
      CompletableFuture@Server<String> f_x2 = ch2.<String>com( x2, 2@Worker2, tok2, 2@Server, tok_s );

      // Server computes results.
      CompletableFuture@Server<String> f_y1 = f_x1.then( x1 -> state_s.compute(x1) );
      CompletableFuture@Server<String> f_y2 = f_x2.then( x2 -> state_s.compute(x2) );

      // Server sends back results.
      CompletableFuture@Client<String> f_y1_w1 = ch3.<String>com( f_y1, 3@Server, tok_s, 3@Client, tok_c );
      CompletableFuture@Client<String> f_y2_w2 = ch3.<String>com( f_y2, 4@Server, tok_s, 4@Client, tok_c );

      // Producers store the data.
      f_y1_w1.thenAccept( y1 -> state1.store(y1) );
      f_y2_w2.thenAccept( y2 -> state2.store(y2) );

      // TODO
      // Implement Worker callbacks
      // Implement Server callbacks
   }
}