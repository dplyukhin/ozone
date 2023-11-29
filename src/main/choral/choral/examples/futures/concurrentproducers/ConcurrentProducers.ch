package choral.examples.futures.concurrentproducers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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
      Token@Server tok_s
   ) { 
      // Workers produce data.
      String@Worker1 x1 = state1.produce();
      String@Worker2 x2 = state2.produce();

      // Workers send data to the server.
      CompletableFuture@Server<String> f_x1 = ch1.<String>com( x1, 1@Worker1, tok1, 1@Server, tok_s );
      CompletableFuture@Server<String> f_x2 = ch2.<String>com( x2, 2@Worker2, tok2, 2@Server, tok_s );

      // Server computes results.
      Function@Server<String,String> s_onData = new ServerOnData@Server(state_s);
      CompletableFuture@Server<String> f_y1 = f_x1.thenApply<String@Server>( s_onData );
      CompletableFuture@Server<String> f_y2 = f_x2.thenApply<String@Server>( s_onData );

      // Server sends back results.
      CompletableFuture@Worker1<String> f_y1_w1 = ch1.<String>com( f_y1, 3@Server, tok_s, 3@Worker1, tok1 );
      CompletableFuture@Worker2<String> f_y2_w2 = ch2.<String>com( f_y2, 4@Server, tok_s, 4@Worker2, tok2 );

      // Producers store the data.
      Consumer@Worker1<String> w1_onData = new WorkerOnData@Worker1(state1);
      f_y1_w1.thenAccept(w1_onData);
      Consumer@Worker2<String> w2_onData = new WorkerOnData@Worker2(state2);
      f_y2_w2.thenAccept(w2_onData);
   }
}