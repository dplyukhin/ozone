package choral.examples.ozone.concurrentbuyers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Consumer;

import choral.channels.AsyncChannel;
import choral.runtime.Token;

/** 
 * A choreography in which two clients concurrently attempt to buy items
 * from a server, which represents an online shop. The choreography is
 * similar to [[choral.examples.ozone.concurrentproducers]], but uses
 * a choreographic procedure [[BuyIt]]. This shows how processes need to
 * create fresh tokens whenever a procedure is called.
 */
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
      BuyIt@(Client1, Server) chor1 = new BuyIt@(Client1, Server)();
      chor1.buyItem( ch1, state_c1, state_s, "Sound and Fury"@Client1, tok_c1.nextToken( 0@Client1 ), tok_s.nextToken( 0@Server ));

      BuyIt@(Client2, Server) chor2 = new BuyIt@(Client2, Server)();
      chor2.buyItem( ch2, state_c2, state_s, "My Year of Rest and Relaxation"@Client2, tok_c2.nextToken( 1@Client2 ), tok_s.nextToken( 1@Server ));
   }
}
