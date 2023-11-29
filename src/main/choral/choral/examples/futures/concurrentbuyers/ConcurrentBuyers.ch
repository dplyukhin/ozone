package choral.examples.futures.concurrentbuyers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Consumer;

import choral.runtime.AsyncChannel;
import choral.runtime.Token;

public class ConcurrentBuyers@( Client1, Client2, Server ) {

	public void go( 
      AsyncChannel@( Client1, Server )< String > ch1, 
      AsyncChannel@( Client2, Server )< String > ch2, 
      ClientState@Client1 state_c1,
      ClientState@Client2 state_c2,
      ServerState@Server state_s,
      Token@Client1 tok_c1,
      Token@Client2 tok_c2,
      Token@Server tok_s
   ) { 
   }
}